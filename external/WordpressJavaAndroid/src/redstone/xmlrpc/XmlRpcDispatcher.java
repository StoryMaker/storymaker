/*
    Copyright (c) 2005 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
*/

package redstone.xmlrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/**
 *  Objects of the XmlRpcDispather class perform the parsing of inbound XML-RPC
 *  messages received by an XmlRpcServer and are responsible for invoking handlers and
 *  dealing with their return values and exceptions.
 *
 *  @author  Greger Olsson
 */

public class XmlRpcDispatcher extends XmlRpcParser
{
    public static String DEFAULT_HANDLER_NAME = "__default__";

    /**
     *  Creates a dispatcher and associates it with an XmlRpcServer and the IP address
     *  of the calling client.
     *
     *  @param server
     */
    
    public XmlRpcDispatcher( XmlRpcServer server, String callerIp )
    {
        this.server = server;
        this.callerIp = callerIp;
    }
    
    
    /**
     *  Returns the IP adress of the client being dispatched.
     * 
     *  @return The IP adress of the client being dispatched.
     */

    public String getCallerIp()
    {
        return callerIp;
    }
    

    /**
     *  Inbound XML-RPC messages to a server are delegated to this method. It
     *  performs the parsing of the message, through the inherited parse() method,
     *  and locates and invokes the appropriate invocation handlers.
     *
     *  @throws XmlRpcException When the inbound XML message cannot be parsed due to no
     *                   available SAX driver, or when an invalid message was received.
     *                   All other exceptions are caught and encoded within the
     *                   XML-RPC writer.
     */

    public void dispatch( InputStream xmlInput, Writer xmlOutput ) throws XmlRpcException
    {
        // Parse the inbound XML-RPC message. May throw an exception.

        parse( xmlInput );

        // Response is written directly to the Writer supplied by the XmlRpcServer.
        
        this.writer = xmlOutput;

        // Exceptions will from hereon be encoded in the XML-RPC response.

        int separator = methodName.lastIndexOf( "." );

        if ( separator == -1 )
        {
            methodName = DEFAULT_HANDLER_NAME + "." + methodName;
            separator = DEFAULT_HANDLER_NAME.length();
        }

        final String handlerName = methodName.substring( 0, separator );
        methodName = methodName.substring( separator + 1 );

        XmlRpcInvocationHandler handler = server.getInvocationHandler( handlerName );

        if ( handler != null )
        {
            final int callId = ++callSequence;
            XmlRpcInvocation invocation = null;
            
            if( server.getInvocationInterceptors().size() > 0 )
            {
                invocation = new XmlRpcInvocation(
                    callId,
                    handlerName,
                    methodName,
                    handler,
                    arguments,
                    writer );
            }
            
            try
            {
                // Invoke the method, which may throw any kind of exception. If any of the
                // preProcess calls thinks the invocation should be cancelled, we do so.

                if ( !preProcess( invocation ) )
                {
                    writeError( -1, XmlRpcMessages.getString( "XmlRpcDispatcher.InvocationCancelled" ) );
                }
                else
                {
                    Object returnValue = handler.invoke( methodName, arguments );
                    returnValue = postProcess( invocation, returnValue );
                    
                    // If the return value wasn't intercepted by any of the interceptors,
                    // write the response using the current serlialization mechanism.
                    
                    if ( returnValue != null )
                    {
                        writeValue( returnValue );
                    }
                }
            }
            catch ( Throwable t )
            {
                processException( invocation, t );
                
                int code = -1;
                if ( t instanceof XmlRpcFault )
                {
                    code = ( (XmlRpcFault) t ).getErrorCode();
                }
                
                writeError( code, t.getClass().getName() + ": " + t.getMessage() );
            }
        }
        else
        {
            writeError( -1, XmlRpcMessages.getString( "XmlRpcDispatcher.HandlerNotFound" ) );
        }
    }


    /**
     *  Override the endElement() method of the XmlRpcParser class, and catch
     *  the method name element. The method name element is unique for XML-RPC
     *  calls, and belongs here in the server.
     */

    public void endElement(
        String uri,
        String name,
        String qualifiedName )
        throws SAXException
    {
        if ( name.equals( "methodName" ) )
        {
            methodName = this.consumeCharData();
        }
        else
        {
            super.endElement( uri, name, qualifiedName );
        }
    }


    /**
     *  Implementation of abstract method introduced in XmlRpcParser. It will
     *  be called whenever a value is parsed during a parse() call. In this
     *  case, the parsed values represent arguments to be sent to the invocation
     *  handler of the call.
     */

    protected void handleParsedValue( Object value )
    {
        arguments.add( value );
    }


    /**
     *  Invokes all processor objects registered with the XmlRpcServer this dispatcher is
     *  working for.
     *
     *  @todo Determine a way for a preProcess call to indicate the reason for cancelling
     *        the invocation.
     *
     *  @return true if the invocation should continue, or false if the invocation should
     *          be cancelled for some reason.
     */

    private boolean preProcess( XmlRpcInvocation invocation )
    {
        XmlRpcInvocationInterceptor p;

        for ( int i = 0; i < server.getInvocationInterceptors().size(); ++i )
        {
            p = ( XmlRpcInvocationInterceptor ) server.getInvocationInterceptors().get( i );

            if ( !p.before( invocation ) )
            {
                return false;
            }
        }

        return true;
    }

    
    /**
     *  Invokes all interceptor objects registered with the XmlRpcServer this dispatcher is
     *  working for.
     */

    private Object postProcess( XmlRpcInvocation invocation, Object returnValue )
    {
        XmlRpcInvocationInterceptor p;

        for ( int i = 0; i < server.getInvocationInterceptors().size(); ++i )
        {
            p = ( XmlRpcInvocationInterceptor ) server.getInvocationInterceptors().get( i );
            returnValue = p.after( invocation, returnValue );
            
            // If the interceptor intercepts the return value completely and takes
            // responsibility for writing a response directly to the client, break
            // the interceptor chain and return immediately.
            
            if ( returnValue == null )
            {
                return null;
            }
        }
        
        return returnValue;
    }


    /**
     *  Invokes all processor objects registered with the XmlRpcServer this dispatcher is
     *  working for.
     */

    private void processException(
        XmlRpcInvocation invocation,
        Throwable exception )
    {
        XmlRpcInvocationInterceptor p;

        for ( int i = 0; i < server.getInvocationInterceptors().size(); ++i )
        {
            p = ( XmlRpcInvocationInterceptor ) server.getInvocationInterceptors().get( i );

            p.onException( invocation, exception );
        }
    }


    /**
     *  Writes a return value to the XML-RPC writer.
     *
     *  @param value The value to be encoded into the writer.
     * @throws IOException 
     */

    private void writeValue( Object value ) throws IOException
    {
        server.getSerializer().writeEnvelopeHeader( value, writer );
        
        if ( value != null )
        {
            server.getSerializer().serialize( value , writer );
        }
        
        server.getSerializer().writeEnvelopeFooter( value, writer );
    }


    /**
     *  Creates an XML-RPC fault struct and puts it into the writer buffer.
     *
     *  @param code The fault code.
     *  @param message The fault string.
     */

    private void writeError( int code, String message )
    {
        try
        {
            logger.log( Level.WARNING, message );
            this.server.getSerializer().writeError( code, message, writer );
        }
        catch ( IOException ignore )
        {
            // If an exception occurs at this point there is no way to recover.
            // We are already trying to send a fault to the client. We swallow
            // the exception and trace it to the console.
            
            logger.log(
                Level.SEVERE,
                XmlRpcMessages.getString( "XmlRpcDispatcher.ErrorSendingFault" ),
                ignore );
        }
    }


    /** The XmlRpcServer this dispatcher is working for */
    private XmlRpcServer server;

    /** The IP address of the caller */
    private String callerIp;

    /** The name of the method the client wishes to call */
    private String methodName;

    /** The arguments for the method */
    private List arguments = new ArrayList( 6 );

    /** Holds the XML-RPC repsonse as it is built up */
    private Writer writer;
    
    /** The current call sequence for traceability */
    private static int callSequence;
    
    /** Logger used to log problems an exceptions. */
    private static Logger logger = Logger.getLogger( XmlRpcDispatcher.class.getName() );
}

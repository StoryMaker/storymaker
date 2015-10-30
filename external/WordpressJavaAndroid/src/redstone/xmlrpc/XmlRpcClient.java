/*
    Copyright (c) 2007 Redstone Handelsbolag

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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.AbstractHttpEntity;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.message.BasicHeader;
import ch.boye.httpclientandroidlib.protocol.HTTP;
import info.guardianproject.netcipher.client.StrongHttpsClient;

/**
 *  An XmlRpcClient represents a connection to an XML-RPC enabled server. It
 *  implements the XmlRpcInvocationHandler so that it may be used as a relay
 *  to other XML-RPC servers when installed in an XmlRpcServer. 
 *
 *  @author Greger Olsson
 */

public class XmlRpcClient extends XmlRpcParser implements XmlRpcInvocationHandler
{
    /**
     *  Creates a new client with the ability to send XML-RPC messages
     *  to the the server at the given URL.
     *
     *  @param url the URL at which the XML-RPC service is locaed
     * 
     *
     * @throws MalformedURLException 
     */

    
    /**
     *
     */

    public XmlRpcClient( URL url)
    {
        this.url = url;
        writer = new StringWriter( 2048 );
        
    }


    /**
     *  Sets the HTTP request properties that the client will use for the next invocation,
     *  and any invocations that follow until setRequestProperties() is invoked again. Null
     *  is accepted and means that no special HTTP request properties will be used in any
     *  future XML-RPC invocations using this XmlRpcClient instance.
     *
     *  @param requestProperties The HTTP request properties to use for future invocations
     *                           made using this XmlRpcClient instance. These will replace
     *                           any previous properties set using this method or the
     *                           setRequestProperty() method.
     */

    public void setRequestProperties( Map requestProperties )
    {
        this.requestProperties = requestProperties;
    }
    

    /**
     *  Sets a single HTTP request property to be used in future invocations.
     *  @see #setRequestProperties(Map)
     *
     *  @param name Name of the property to set
     *  @param value The value of the property
     */

    public void setRequestProperty( String name, String value )
    {
        if ( requestProperties == null )
        {
            requestProperties = new HashMap();
        }
        
        requestProperties.put( name, value );
    }
    

    /**
     *  Invokes a method on the terminating XML-RPC end point. The supplied method name and
     *  argument collection is used to encode the call into an XML-RPC compatible message.
     *
     *  @param method The name of the method to call.
     *
     *  @param arguments The arguments to encode in the call.
     *
     *  @return The object returned from the terminating XML-RPC end point.
     *
     *  @throws XmlRpcException One or more of the supplied arguments are unserializable. That is,
     *                          the built-in serializer connot parse it or find a custom serializer
     *                          that can. There may also be problems with the socket communication.
     * @throws  XmlRpcFault Error occurred in the method call.
     */

    public synchronized Object invoke(
        String method,
        List arguments )
        throws XmlRpcException, XmlRpcFault
    {
        beginCall( method );

        if ( arguments != null )
        {
            Iterator argIter = arguments.iterator();

            while ( argIter.hasNext() )
            {
                try
                {
                    writer.write( "<param>" );
                    serializer.serialize( argIter.next(), writer );
                    writer.write( "</param>" );
                }
                catch ( IOException ioe )
                {
                    throw new XmlRpcException(
                        XmlRpcMessages.getString( "XmlRpcClient.NetworkError" ), ioe );
                }
            }
        }

        return endCall();
    }


    /**
     *  Invokes a method on the terminating XML-RPC end point. The supplied method name and
     *  argument vector is used to encode the call into XML-RPC.
     *
     *  @param method The name of the method to call.
     *
     *  @param arguments The arguments to encode in the call.
     *
     *  @return The object returned from the terminating XML-RPC end point.
     *
     *  @throws XmlRpcException One or more of the supplied arguments are unserializable. That is,
     *                          the built-in serializer connot parse it or find a custom serializer
     *                          that can. There may also be problems with the socket communication.
     */

    public synchronized Object invoke(
        String method,
        Object[] arguments )
        throws XmlRpcException, XmlRpcFault
    {
        beginCall( method );

        if ( arguments != null )
        {
            for ( int i = 0; i < arguments.length; ++i )
            {
                try
                {
                    writer.write( "<param>" );
                    serializer.serialize( arguments[ i ], writer );
                    writer.write( "</param>" );
                }
                catch ( IOException ioe )
                {
                    throw new XmlRpcException(
                        XmlRpcMessages.getString( "XmlRpcClient.NetworkError" ), ioe );
                }
            }
        }

        return endCall();
    }


    /**
     *  Returns the HTTP header fields from the latest server invocation.
     *  These are the fields set by the HTTP server hosting the XML-RPC service.
     * 
     *  @return The HTTP header fields from the latest server invocation. Note that
     *          the XmlRpcClient instance retains ownership of this map and the map
     *          contents is replaced on the next request. If there is a need to
     *          keep the fields between requests the map returned should be cloned.
     */

    public Map getResponseHeaderFields()
    {
        return headerFields;
    }
    

    /**
     *  A asynchronous version of invoke performing the call in a separate thread and
     *  reporting responses, faults, and exceptions through the supplied XmlRpcCallback.
     *  TODO Determine on proper strategy for instantiating Threads.
     *
     *  @param method The name of the method at the server.
     *
     *  @param arguments The arguments for the call. This may be either a java.util.List
     *                   descendant, or a java.lang.Object[] array.
     *
     *  @param callback An object implementing the XmlRpcCallback interface. If callback is
     *                  null, the call will be performed but any results, faults, or exceptions
     *                  will be ignored (fire and forget).
     */

    public void invokeAsynchronously(
        final String method,
        final Object arguments,
        final XmlRpcCallback callback )
    {
        if ( callback == null )
        {
            new Thread()
            {
                public void run()
                {
                    try // Just fire and forget.
                    {
                        if ( arguments instanceof Object[] )
                            invoke( method, ( Object[] ) arguments );
                        else
                            invoke( method, ( List ) arguments );
                    }
                    catch ( XmlRpcFault e ) { /* Ignore, no callback. */ }
                    catch ( XmlRpcException e ) { /* Ignore, no callback. */ }
                }
            }.start();
        }
        else
        {
            new Thread()
            {
                public void run()
                {
                    Object result = null;

                    try
                    {
                        if ( arguments instanceof Object[] )
                            result = invoke( method, ( Object[] ) arguments );
                        else
                            result = invoke( method, ( List ) arguments );

                        callback.onResult( result );
                    }
                    catch ( XmlRpcException e )
                    {
                        callback.onException( e );
                    }
                    catch ( XmlRpcFault e )
                    {
                        XmlRpcStruct fault = ( XmlRpcStruct ) result;
    
                        callback.onFault( fault.getInteger( "faultCode" ),
                                          fault.getString( "faultString" ) );
                    }                    
                }
            }.start();
        }
    }


    /**
     *  Initializes the XML buffer to be sent to the server with the XML-RPC
     *  content common to all method calls, or serializes it directly over the writer
     *  if streaming is used. The parameters to the call are added in the execute()
     *  method, and the closing tags are appended when the call is finalized in endCall().
     *
     *  @param methodName The name of the method to call.
     */

    private void beginCall( String methodName ) throws XmlRpcException
    {
        try
        {
            ( ( StringWriter ) writer ).getBuffer().setLength( 0 );
            
            writer.write( "<?xml version=\"1.0\" encoding=\"" );
            writer.write( XmlRpcMessages.getString( "XmlRpcClient.Encoding" ) );
            writer.write( "\"?>" );
            writer.write( "<methodCall><methodName>" );
            writer.write( methodName );
            writer.write( "</methodName><params>" );
            
            
        }
        catch( IOException ioe )
        {
            throw new XmlRpcException(
                XmlRpcMessages.getString( "XmlRpcClient.NetworkError" ), ioe );
        }
    }


    /**
     *  Finalizaes the XML buffer to be sent to the server, and creates a HTTP buffer for
     *  the call. Both buffers are combined into an XML-RPC message that is sent over
     *  a socket to the server.
     *
     *  @return The parsed return value of the call.
     *
     *  @throws XmlRpcException when some IO problem occur.
     */

    private Object endCall() throws XmlRpcException, XmlRpcFault
    {
        try
        {
            writer.write( "</params>" );
            writer.write( "</methodCall>" );

            String xmlOut = ( ( StringWriter ) writer ).getBuffer().toString();
            StringEntity entity = new StringEntity(xmlOut, HTTP.UTF_8);
            entity.setContentType(
            		new BasicHeader("Content-Type", "text/xml; charset=" + XmlRpcMessages.getString( "XmlRpcClient.Encoding" ))
            );

            Log.d("XmlRpc",xmlOut);
            hResp = openConnection(entity);
       
            handleResponse();
        }
        catch ( IOException ioe )
        {
            throw new XmlRpcException(
                XmlRpcMessages.getString( "XmlRpcClient.NetworkError" ),
                    ioe );
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch( IOException ignore ) { /* Closed or not, we don't care at this point. */ }
            
        }
        
        return returnValue;
    }


    /**
     *  Handles the response returned by the XML-RPC server. If the server responds with a
     *  "non-200"-HTTP response or if the XML payload is unparseable, this is interpreted
     *  as an error in communication and will result in an XmlRpcException.<p>
     *
     *  If the user does not want the socket to be kept alive or if the server does not
     *  support keep-alive, the socket is closed.
     *
     *  @throws IOException If a socket error occurrs, or if the XML returned is unparseable.
     *                      This exception is currently also thrown if a HTTP response other
     *                      than "200 OK" is received.
     * @throws XmlRpcFault 
     */

    private void handleResponse() throws XmlRpcFault
    {
        try
        {
            parse( new BufferedInputStream( hResp.getEntity().getContent()) );

            headerFields.clear();

            Header[] headers = hResp.getAllHeaders();
            
            for (Header header : headers)
            {
                headerFields.put( header.getName(), header.getValue() );
	
            }
        }
        catch ( Exception e )
        {
            throw new XmlRpcException(
                XmlRpcMessages.getString( "XmlRpcClient.ParseError" ), e );
        }
        
        if ( isFaultResponse )
        {
            XmlRpcStruct fault = ( XmlRpcStruct ) returnValue;
            isFaultResponse = false;
            
            throw new XmlRpcFault( fault.getInteger( "faultCode" ),
                                   fault.getString( "faultString" ) );
        }
    }


    /**
     *  Override the startElement() method inherited from XmlRpcParser. This way, we may set
     *  the error flag if we run into a fault-tag.
     *
     *  @param uri {@inheritDoc}
     *  @param name {@inheritDoc}
     *  @param qualifiedName {@inheritDoc}
     *  @param attributes {@inheritDoc}
     */

    public void startElement(
        String uri,
        String name,
        String qualifiedName,
        Attributes attributes )
        throws SAXException
    {
        if ( name.equals( "fault" ) )
        {
            isFaultResponse = true;
        }
        else
        {
            super.startElement( uri, name, qualifiedName, attributes );
        }
    }


    /**
     *  Stores away the one and only value contained in XML-RPC responses.
     *
     *  @param value The contained return value.
     */

    protected void handleParsedValue( Object value )
    {
        returnValue = value;
    }


    /**
     *  Opens a connection to the URL associated with the client instance. Any
     *  HTTP request properties set using setRequestProperties() are recorded
     *  with the internal HttpURLConnection and are used in the HTTP request.
     * 
     *  @throws IOException If a connection could not be opened. The exception
     *                      is propagated out of any unsuccessful calls made into
     *                      the internal java.net.HttpURLConnection.
     */

    private HttpResponse openConnection(AbstractHttpEntity entity) throws IOException
    {
    	StrongHttpsClient httpClient = new StrongHttpsClient(mContext);

		if (mUseProxy)
		{
			httpClient.useProxy(true, mProxyType, mProxyHost, mProxyPort);
		}
		
		HttpPost request = new HttpPost(url.toExternalForm());
		
		request.setHeader("Content-Type", "text/xml; charset=" +
	            XmlRpcMessages.getString( "XmlRpcClient.Encoding" ));
		
		entity.setContentEncoding(XmlRpcMessages.getString( "XmlRpcClient.Encoding" ));
		entity.setContentType("text/xml; charset=" +
	            XmlRpcMessages.getString( "XmlRpcClient.Encoding" ));
		
		
        if ( requestProperties != null )
        {
            for ( Iterator propertyNames = requestProperties.keySet().iterator();
                  propertyNames.hasNext(); )
            {
                String propertyName = ( String ) propertyNames.next();
                
               request.setHeader(
                    propertyName,
                    ( String ) requestProperties.get( propertyName ) );
            }
        }
        
        
	if (entity != null)
		request.setEntity(entity);
	
        
        return httpClient.execute(request);
    }
    
    public static void setContext (Context context)
    {
    	mContext = context;
    }
    
    public static void setProxy (boolean useProxy, String proxyType, String proxyHost, int proxyPort)
    {
    	mUseProxy = useProxy;
    	mProxyType = proxyType;
    	mProxyHost = proxyHost;
    	mProxyPort = proxyPort;
    }
    
    /** The server URL. */
    private URL url;

    /** HTTP Resposne **/
    private HttpResponse hResp = null; //latest response
    
    /** The Android App Context **/
    private static Context mContext;
    
    /** Proxy Settings **/
    private static boolean mUseProxy = false;
    private static String mProxyType = null;
    private static String mProxyHost = null;
    private static int mProxyPort = -1;
    
    /** HTTP request properties, or null if none have been set by the application. */
    private Map requestProperties;
    
    /** HTTP header fields returned by the server in the latest response. */
    private Map headerFields = new HashMap();
    
    /** The parsed value returned in a response. */
    private Object returnValue;

    /** Writer to which XML-RPC messages are serialized. */
    private Writer writer;
    
    /** Indicates whether or not the incoming response is a fault response. */
    private boolean isFaultResponse;
    
    /** The serializer used to serialize arguments. */
    private XmlRpcSerializer serializer = new XmlRpcSerializer();
}

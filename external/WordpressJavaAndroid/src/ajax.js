/**
 *  Creates a connection object that may be used to post messages
 *  to a server. 
 */
function Connection()
{
    var self = this;

    /**
     *  Returns an XmlHttpRequest object for the current browser.
     */
    this.getXmlHttpRequest = function()
    {
        var result = null;

        try
        {
            result = new XMLHttpRequest();
        }
        catch ( e )
        {
            try
            {
                result = new ActiveXObject( 'Msxml2.XMLHTTP' )
            }
            catch( e )
            {
                var success = false;
                
                var MSXML_XMLHTTP_PROGIDS = new Array(
                    'Microsoft.XMLHTTP',
                    'MSXML2.XMLHTTP',
                    'MSXML2.XMLHTTP.5.0',
                    'MSXML2.XMLHTTP.4.0',
                    'MSXML2.XMLHTTP.3.0' );
                
                for ( var i = 0; i < MSXML_XMLHTTP_PROGIDS.length && !success; i++ )
                {
                    try
                    {
                        result = new ActiveXObject( MSXML_XMLHTTP_PROGIDS[ i ] );
                        success = true;
                    }
                    catch ( e )
                    {
                        result = null;
                    }
                }
            }
        }
        
        self.xmlHttpRequest = result;
        return result;
    }
    
    /**
     *  Posts a message to a server.
     */
    this.post = function( url, content, contentType )
    {
        if ( typeof self.xmlHttpRequest.abort == 'function' && self.xmlHttpRequest.readyState != 0 )
        {
            self.xmlHttpRequest.abort();
        }

        self.xmlHttpRequest.open( 'POST', url, false );
        
        if ( typeof self.xmlHttpRequest.setRequestHeader == 'function' )
        {
            self.xmlHttpRequest.setRequestHeader(
                'Content-Type',
                contentType );
        }

        self.xmlHttpRequest.send( content );
        return self.xmlHttpRequest.responseText;
    }
    
    if ( !this.getXmlHttpRequest() )
    {
        throw new Error( "Could not load XMLHttpRequest object" );
    }
}

/**
 *  Client object constructor.
 */
function AjaxService( url, handlerName )
{
    this.url = url;
    this.handlerName = handlerName;
    this.connection = new Connection();
}

/**
 *  Posts an XML-RPC message to the supplied method using the
 *  given arguments.
 */
AjaxService.prototype.invoke = function( method, arguments )
{
    return this.connection.post( this.url, this.getMessage( method, arguments ), 'text/xml' );
}

/**
 *  Generates an XML-RPC message based on the supplied method name
 *  and argument array.
 */
AjaxService.prototype.getMessage = function( method, arguments )
{
    if ( arguments == null )
    {
        arguments = new Array();
    }

    var message =
        '<?xml version="1.0"?><methodCall><methodName>' +
        this.handlerName + '.' + method +
        '</methodName>';
                
    if ( arguments.length > 0 )
    {
        message += '<params>';
        
        for ( var i = 0; i < arguments.length; i++ )
        {
            var argument = arguments[ i ];
            message += '<param><value>' + argument.serialize() + '</value></param>';
        }
        
        message += '</params>';
    }

    message += '</methodCall>';
    return message;
}

/**
 *  Serialization functions for the String datatype.
 */
String.prototype.serialize = function()
{
    return '<string>' + this + '</string>';
}

/**
 *  Serialization functions for the Number datatype.
 */
Number.prototype.serialize = function()
{
    if ( this % 1 != 0 ) // Very crude way of determining type. May be mismatch at server.
    {
        return '<double>' + this + '</double>';
    }
    else
    {
        return '<i4>' + this + '</i4>';
    }
}

/**
 *  Serialization function for the Boolean datatype.
 */
Boolean.prototype.serialize = function()
{
    return '<boolean>' + this + '</boolean>';
}

/**
 *  Serialization function for the Array datatype. It reuses the
 *  other serialization functions to serialize the members of the array.
 */
Array.prototype.serialize = function()
{
    var result = '<array><data>';

    for ( var i = 0; i < this.length; i++ )
    {
        result += '<value>' + this[ i ].serialize() + '</value>';
    }

    result += '</data></array>';
    return result;
}

/**
 *  Serialization function for Date datatype.
 */
Date.prototype.serialize = function()
{
    return '<dateTime.iso8601>' +
           this.getFullYear() +
           ( this.getMonth() < 10 ? '0' : '' ) + this.getMonth() +
           ( this.getDay() < 10 ? '0' : '' ) + this.getDay() + 'T' +
           ( this.getHours() < 10 ? '0' : '' ) + this.getHours() + ':' +
           ( this.getMinutes() < 10 ? '0' : '' ) + this.getMinutes() + ':' +
           ( this.getMinutes() < 10 ? '0' : '' ) + this.getSeconds() + 
           '</dateTime.iso8601>'; // Phew :-)
}

/**
 *  Serialization functions for the complex datatypes. It reuses the
 *  other serialization functions to serialize the members of the object.
 */
Object.prototype.serialize = function()
{
    var result = '<struct>';

    for ( var member in this )
    {
        if ( typeof( this[ member ] ) != 'function' )
        {
            result += '<member>';
            result += '<name>' + member + '</name>';
            result += '<value>' + this[ member ].serialize() + '</value>';
            result += '</member>';
        }
    }

    result += '</struct>';
    return result;
}
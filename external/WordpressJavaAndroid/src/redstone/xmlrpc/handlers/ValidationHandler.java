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

package redstone.xmlrpc.handlers;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import redstone.xmlrpc.handlers.ReflectiveInvocationHandler;

/**
 *  Handler for the XML-RPC validation suite. This suite is used to verify
 *  the implementation using the verification service at http://www.xmlrpc.com.
 *
 *  @author Greger Olsson
 */

public class ValidationHandler extends ReflectiveInvocationHandler
{
    /**
     *  This handler takes a single parameter, an array of structs, each of which contains at least three elements
     *  named moe, larry and curly, all <i4>s. Your handler must add all the struct elements named curly and
     *  return the result.
     */

    public int arrayOfStructsTest( List structs )
    {
        int result = 0;

        for ( int i = 0; i < structs.size(); ++i )
        {
            Map struct = ( Map ) structs.get( i );
            result += ( ( Integer ) struct.get( "curly" ) ).intValue();
        }

        return result;
    }


    /**
     *  This handler takes a single parameter, a string, that contains any number of predefined entities, namely <,
     *  >, &, ' and ". Your handler must return a struct that contains five fields, all numbers: ctLeftAngleBrackets,
     *  ctRightAngleBrackets, ctAmpersands, ctApostrophes, ctQuotes. To validate, the numbers must be correct.
     */

    public Map countTheEntities( String str )
    {
        int ctLeftAngleBrackets  = 0;
        int ctRightAngleBrackets = 0;
        int ctAmpersands         = 0;
        int ctApostrophes        = 0;
        int ctQuotes             = 0;

        for ( int i = 0; i < str.length(); ++i )
        {
            switch ( str.charAt( i ) )
            {
                case '<':  ++ctLeftAngleBrackets;  break;
                case '>':  ++ctRightAngleBrackets; break;
                case '&':  ++ctAmpersands;         break;
                case '\'': ++ctApostrophes;        break;
                case '\"': ++ctQuotes;             break;
            }
        }

        Map/*<String,Integer>*/ result = new HashMap();

        result.put( "ctLeftAngleBrackets", new Integer( ctLeftAngleBrackets ) );
        result.put( "ctRightAngleBrackets", new Integer( ctRightAngleBrackets ) );
        result.put( "ctAmpersands", new Integer( ctAmpersands ) );
        result.put( "ctApostrophes", new Integer( ctApostrophes ) );
        result.put( "ctQuotes", new Integer( ctQuotes ) );

        return result;
    }


    /**
     *  This handler takes a single parameter, a struct, containing at least three elements named moe, larry and
     *  curly, all <i4>s. Your handler must add the three numbers and return the result.
     */

    public int easyStructTest( Map struct )
    {
        int result = 0;

        result += ( ( Integer ) struct.get( "moe" ) ).intValue();
        result += ( ( Integer ) struct.get( "larry" ) ).intValue();
        result += ( ( Integer ) struct.get( "curly" ) ).intValue();

        return result;
    }


    /**
     *  This handler takes a single parameter, a struct. Your handler must return the struct.
     */

    public Map echoStructTest( Map struct )
    {
        return struct;
    }


    /**
     *  This handler takes six parameters, and returns an array containing all the parameters.
     */

    public List manyTypesTest(
        int number,
        boolean bool,
        String string,
        double dbl,
        Date dateTime,
        byte[] bytes )
    {
        List result = new ArrayList( 6 );

        result.add( new Integer( number ) );
        result.add( new Boolean( bool ) );
        result.add( string );
        result.add( new Double( dbl ) );
        result.add( dateTime );
        result.add( bytes );

        return result;
    }


    /**
     *  This handler takes a single parameter, which is an array containing between 100 and 200 elements. Each
     *  of the items is a string, your handler must return a string containing the concatenated text of the first and
     *  last elements.
     */

    public String moderateSizeArrayCheck( List strings )
    {
        return  ( ( String ) strings.get( 0 ) ) +
                ( ( String ) strings.get( strings.size() - 1 ) );
    }


    /**
     *  This handler takes a single parameter, a struct, that models a daily calendar. At the top level, there is one
     *  struct for each year. Each year is broken down into months, and months into days. Most of the days are
     *  empty in the struct you receive, but the entry for April 1, 2000 contains a least three elements named
     *  moe, larry and curly, all <i4>s. Your handler must add the three numbers and return the result.
     */

    public int nestedStructTest( Map struct )
    {
        int result = 0;

        try
        {
            struct = ( Map ) struct.get( "2000" );
            struct = ( Map ) struct.get( "04" );
            struct = ( Map ) struct.get( "01" );

            result += ( ( Integer ) struct.get( "moe" ) ).intValue();
            result += ( ( Integer ) struct.get( "larry" ) ).intValue();
            result += ( ( Integer ) struct.get( "curly" ) ).intValue();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return result;
    }


    /**
     *  This handler takes one parameter, and returns a struct containing three elements, times10, times100 and
     *  times1000, the result of multiplying the number by 10, 100 and 1000.
     */

    public Map simpleStructReturnTest( int number )
    {
        Map/*<String,Integer>*/ result = new HashMap();

        result.put( "times10", new Integer( number * 10 ) );
        result.put( "times100", new Integer( number * 100 ) );
        result.put( "times1000", new Integer( number * 1000 ) );

        return result;
    }
}
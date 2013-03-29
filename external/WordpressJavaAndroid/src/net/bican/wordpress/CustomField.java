/*
 * 
 * Wordpress-java
 * http://code.google.com/p/wordpress-java/
 * 
 * Copyright 2012 Can Bican <can@bican.net>
 * See the file 'COPYING' in the distribution for licensing terms.
 * 
 */
package net.bican.wordpress;


/**
 * This class represents custom fields in wordpress.
 * 
 * @author alok
 * 
 */
public class CustomField extends XmlRpcMapped implements StringHeader {
  String id = null;

  String key = null;

  String value = null;

  /**
   * (non-Javadoc)
   * 
   * @see net.bican.wordpress.StringHeader#getStringHeader()
   */
  @Override
  @SuppressWarnings("nls")
  public String getStringHeader() {
    final String TAB = ":";
    return "Id" + TAB + "Key" + TAB + "Value";
  }


  /**
   * @return id of custom field
   */
  public String getId() {
    return this.id;
  }

  /**
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return key of custom field
   */
  public String getKey() {
    return this.key;
  }

  /**
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return value of custom field
   */
  public String getValue() {
    return this.value;
  }

  /**
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }



}

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.13 at 02:35:39 PM CDT 
//


package edu.wustl.mir.erl.tagsniffer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}standardElements"/>
 *         &lt;element ref="{}privateElements"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "standardElements",
    "privateElements"
})
@XmlRootElement(name = "rules")
public class Rules {

    @XmlElement(required = true)
    protected StandardElements standardElements;
    @XmlElement(required = true)
    protected PrivateElements privateElements;

    /**
     * Gets the value of the standardElements property.
     * 
     * @return
     *     possible object is
     *     {@link StandardElements }
     *     
     */
    public StandardElements getStandardElements() {
        return standardElements;
    }

    /**
     * Sets the value of the standardElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardElements }
     *     
     */
    public void setStandardElements(StandardElements value) {
        this.standardElements = value;
    }

    /**
     * Gets the value of the privateElements property.
     * 
     * @return
     *     possible object is
     *     {@link PrivateElements }
     *     
     */
    public PrivateElements getPrivateElements() {
        return privateElements;
    }

    /**
     * Sets the value of the privateElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrivateElements }
     *     
     */
    public void setPrivateElements(PrivateElements value) {
        this.privateElements = value;
    }

}

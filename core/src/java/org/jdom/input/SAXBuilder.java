/*-- 

 $Id: SAXBuilder.java,v 1.66 2002/04/28 08:44:29 jhunter Exp $

 Copyright (C) 2000 Jason Hunter & Brett McLaughlin.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <pm AT jdom DOT org>.
 
 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <pm AT jdom DOT org>.
 
 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Jason Hunter <jhunter AT jdom DOT org> and
 Brett McLaughlin <brett AT jdom DOT org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.
 
 */

package org.jdom.input;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.jdom.*;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p><code>SAXBuilder</code> builds a JDOM tree using SAX.
 * Information about SAX can be found at 
 * <a href="http://www.megginson.com/SAX">http://www.megginson.com/SAX</a>.</p>
 *
 * <p>Known issues: Relative paths for a DocType or EntityRef may be
 * converted by the SAX parser into absolute paths</p>
 *
 * @author Jason Hunter
 * @author Brett McLaughlin
 * @author Dan Schaffer
 * @author Philip Nelson
 * @author Alex Rosen
 * @version $Revision: 1.66 $, $Date: 2002/04/28 08:44:29 $
 */
public class SAXBuilder {

    private static final String CVS_ID = 
      "@(#) $RCSfile: SAXBuilder.java,v $ $Revision: 1.66 $ $Date: 2002/04/28 08:44:29 $ $Name:  $";

    /** 
     * Default parser class to use. This is used when no other parser
     * is given and JAXP isn't available.
     */
    private static final String DEFAULT_SAX_DRIVER =
        "org.apache.xerces.parsers.SAXParser";

    /** Whether validation should occur */
    private boolean validate;

    /** Whether expansion of entities should occur */
    private boolean expand = true;

    /** Adapter class to use */
    private String saxDriverClass;

    /** ErrorHandler class to use */
    private ErrorHandler saxErrorHandler = null;
 
    /** EntityResolver class to use */
    private EntityResolver saxEntityResolver = null;

    /** DTDHandler class to use */
    private DTDHandler saxDTDHandler = null;

    /** XMLFilter instance to use */
    private XMLFilter saxXMLFilter = null;
 
    /** The factory for creating new JDOM objects */
    protected JDOMFactory factory = null;

    /** Whether to ignore ignorable whitespace */
    private boolean ignoringWhite = false;

    /** User-specified features to be set on the SAX parser */
    private HashMap features = new HashMap(5);

    /** User-specified properties to be set on the SAX parser */
    private HashMap properties = new HashMap(5);

    /**
     * <p>
     * Creates a new SAXBuilder which will attempt to first locate
     * a parser via JAXP, then will try to use a set of default 
     * SAX Drivers. The underlying parser will not validate.
     * </p>
     */
    public SAXBuilder() {
        this(false);
    }

    /**
     * <p>
     * Creates a new SAXBuilder which will attempt to first locate
     * a parser via JAXP, then will try to use a set of default 
     * SAX Drivers. The underlying parser will validate or not
     * according to the given parameter.
     * </p>
     *
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public SAXBuilder(boolean validate) {
        this.validate = validate;
    }

    /**
     * <p>
     * Creates a new SAXBuilder using the specified SAX parser.
     * The underlying parser will not validate.
     * </p>
     *
     * @param saxDriverClass <code>String</code> name of SAX Driver
     *                       to use for parsing.
     */
    public SAXBuilder(String saxDriverClass) {
        this(saxDriverClass, false);
    }

    /**
     * <p>
     * Creates a new SAXBuilder using the specified SAX parser.
     * The underlying parser will validate or not
     * according to the given parameter.
     * </p>
     *
     * @param saxDriverClass <code>String</code> name of SAX Driver
     *                       to use for parsing.
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public SAXBuilder(String saxDriverClass, boolean validate) {
        this.saxDriverClass = saxDriverClass;
        this.validate = validate;
    }

    /*
     * <p>
     * This sets a custom JDOMFactory for the builder.  Use this to build
     * the tree with your own subclasses of the JDOM classes.
     * </p>
     *
     * @param factory <code>JDOMFactory</code> to use
     */
    public void setFactory(JDOMFactory factory) {
        this.factory = factory;
    }

    /**
     * <p>
     * This sets validation for the builder.
     * </p>
     *
     * @param validate <code>boolean</code> indicating whether validation 
     * should occur.
     */
    public void setValidation(boolean validate) {
        this.validate = validate;
    }

    /**
     * <p>
     * This sets custom ErrorHandler for the <code>Builder</code>.
     * </p>
     *
     * @param errorHandler <code>ErrorHandler</code>
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        saxErrorHandler = errorHandler;
    }

    /**
     * <p>
     * This sets custom EntityResolver for the <code>Builder</code>.
     * </p>
     *
     * @param entityResolver <code>EntityResolver</code>
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        saxEntityResolver = entityResolver;
    }

    /**
     * <p>
     * This sets custom DTDHandler for the <code>Builder</code>.
     * </p>
     *
     * @param dtdHandler <code>DTDHandler</code>
     */
    public void setDTDHandler(DTDHandler dtdHandler) {
        saxDTDHandler = dtdHandler;
    }

    /**
     * <p>
     * This sets custom XMLFilter for the <code>Builder</code>.
     * </p>
     *
     * @param xmlFilter <code>XMLFilter</code>
     */
    public void setXMLFilter(XMLFilter xmlFilter) {
        saxXMLFilter = xmlFilter;
    }
 
    /**
     * <p>
     * Specifies whether or not the parser should elminate whitespace in 
     * element content (sometimes known as "ignorable whitespace") when
     * building the document.  Only whitespace which is contained within
     * element content that has an element only content model will be
     * eliminated (see XML Rec 3.2.1).  For this setting to take effect 
     * requires that validation be turned on.  The default value of this
     * setting is <code>false</code>.
     * </p>
     *
     * @param ignoringWhite Whether to ignore ignorable whitespace
     */
    public void setIgnoringElementContentWhitespace(boolean ignoringWhite) {
        this.ignoringWhite = ignoringWhite;
    }

    /**
     * <p>
     * This sets a feature on the SAX parser. See the SAX documentation for
     * more information.
     * </p>
     * <p>
     * NOTE: SAXBuilder requires that some particular features of the SAX parser be
     * set up in certain ways for it to work properly. The list of such features
     * may change in the future. Therefore, the use of this method may cause
     * parsing to break, and even if it doesn't break anything today it might 
     * break parsing in a future JDOM version, because what JDOM parsers require 
     * may change over time. Use with caution.
     * </p>
     *
     * @param name The feature name, which is a fully-qualified URI.
     * @param value The requested state of the feature (true or false).
     */
    public void setFeature(String name, boolean value) {
        // Save the specified feature for later.
        features.put(name, new Boolean(value));
    }

    /**
     * <p>
     * This sets a property on the SAX parser. See the SAX documentation for
     * more information.
     * </p>
     * <p>
     * NOTE: SAXBuilder requires that some particular properties of the SAX parser be
     * set up in certain ways for it to work properly. The list of such properties
     * may change in the future. Therefore, the use of this method may cause
     * parsing to break, and even if it doesn't break anything today it might 
     * break parsing in a future JDOM version, because what JDOM parsers require 
     * may change over time. Use with caution.
     * </p>
     *
     * @param name The property name, which is a fully-qualified URI.
     * @param value The requested value for the property.
     */
    public void setProperty(String name, Object value) {
        // Save the specified property for later.
        properties.put(name, value);
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   input source.
     * </p>
     *
     * @param in <code>InputSource</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(InputSource in) 
     throws JDOMException, IOException {
        SAXHandler contentHandler = null;

        try {
            // Create and configure the content handler.
            contentHandler = createContentHandler();
            configureContentHandler(contentHandler);

            // Create and configure the parser.
            XMLReader parser = createParser();

            // Install optional filter
            if (saxXMLFilter != null) {
                // Connect filter chain to parser
                XMLFilter root = saxXMLFilter;
                while (root.getParent() instanceof XMLFilter) {
                    root = (XMLFilter)root.getParent();
                }
                root.setParent(parser);

                // Read from filter
                parser = saxXMLFilter;
            }

            // Configure parser
            configureParser(parser, contentHandler);

            // Parse the document.
            parser.parse(in);

            return contentHandler.getDocument();
        }
        catch (SAXParseException e) {
          String systemId = e.getSystemId();
          if (systemId != null) {
              throw new JDOMException("Error on line " + 
                e.getLineNumber() + " of document " + systemId, 
                e);
          } else {
              throw new JDOMException("Error on line " +
                              e.getLineNumber(), e);
          }
        }
        catch (SAXException e) {
            throw new JDOMException("Error in building: " + 
                e.getMessage(), e);
        }
        finally {
            // Explicitly nullify the handler to encourage GC
            // It's a stack var so this shouldn't be necessary, but it
            // seems to help on some JVMs
            contentHandler = null;
        }
    }

    /**
     * <p>
     * This creates the SAXHandler that will be used to build the Document.
     * </p>
     *
     * @return <code>SAXHandler</code> - resultant SAXHandler object.
     */
    protected SAXHandler createContentHandler() {
        SAXHandler contentHandler = new SAXHandler(factory);
        return contentHandler;
    }

    /**
     * <p>
     * This configures the SAXHandler that will be used to build the Document.
     * </p>
     * <p>
     * The default implementation simply passes through some configuration
     * settings that were set on the SAXBuilder: setExpandEntities() and
     * setIgnoringElementContentWhitespace().
     * </p>
     */
    protected void configureContentHandler(SAXHandler contentHandler) {
        // Setup pass through behavior
        contentHandler.setExpandEntities(expand);
        contentHandler.setIgnoringElementContentWhitespace(ignoringWhite);
    }

    /**
     * <p>
     * This creates the XMLReader to be used for reading the XML document.
     * </p>
     * <p>
     * The default behavior is to (1) use the saxDriverClass, if it has been
     * set, (2) try to obtain a parser from JAXP, if it is available, and 
     * (3) if all else fails, use a hard-coded default parser (currently
     * the Xerces parser). Subclasses may override this method to determine
     * the parser to use in a different way.
     * </p>
     *
     * @return <code>XMLReader</code> - resultant XMLReader object.
     */
    protected XMLReader createParser() throws JDOMException {
        XMLReader parser = null;
        if (saxDriverClass != null) {
            // The user knows that they want to use a particular class
            try {
              parser = XMLReaderFactory.createXMLReader(saxDriverClass);
              // System.out.println("using specific " + saxDriverClass);
            }
            catch (SAXException e) {
              throw new JDOMException("Could not load " + saxDriverClass, e); 
            }
        } else {
            // Try using JAXP...
            // Note we need JAXP 1.1, and if JAXP 1.0 is all that's
            // available then the getXMLReader call fails and we skip
            // to the hard coded default parser
            try {
                Class factoryClass = 
                    Class.forName("javax.xml.parsers.SAXParserFactory");

                // factory = SAXParserFactory.newInstance();
                Method newParserInstance = 
                    factoryClass.getMethod("newInstance", null);
                Object factory = newParserInstance.invoke(null, null);

                // factory.setValidating(validate);
                Method setValidating = 
                    factoryClass.getMethod("setValidating", 
                                           new Class[]{boolean.class});
                setValidating.invoke(factory, 
                                     new Object[]{new Boolean(validate)});

                // jaxpParser = factory.newSAXParser();
                Method newSAXParser = 
                    factoryClass.getMethod("newSAXParser", null);
                Object jaxpParser  = newSAXParser.invoke(factory, null);

                // parser = jaxpParser.getXMLReader();
                Class parserClass = jaxpParser.getClass();
                Method getXMLReader = 
                    parserClass.getMethod("getXMLReader", null);
                parser = (XMLReader)getXMLReader.invoke(jaxpParser, null);

                // System.out.println("Using jaxp " +
                //   parser.getClass().getName());
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            } catch (InvocationTargetException e) {
                //e.printStackTrace();
            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
        }

        // Check to see if we got a parser yet, if not, try to use a
        // hard coded default
        if (parser == null) {
            try {
                parser = XMLReaderFactory.createXMLReader(DEFAULT_SAX_DRIVER);
                // System.out.println("using default " + DEFAULT_SAX_DRIVER);
                saxDriverClass = parser.getClass().getName();
            }
            catch (SAXException e) {
                throw new JDOMException("Could not load default SAX parser: "
                  + DEFAULT_SAX_DRIVER, e); 
            }
        }

        return parser;
    }

    /**
     * <p>
     * This configures the XMLReader to be used for reading the XML document.
     * </p>
     * <p>
     * The default implementation sets various options on the given XMLReader,
     *  such as validation, DTD resolution, entity handlers, etc., according
     *  to the options that were set (e.g. via <code>setEntityResolver</code>)
     *  and set various SAX properties and features that are required for JDOM
     *  internals. These features may change in future releases, so change this
     *  behavior at your own risk.
     * </p>
     */
    protected void configureParser(XMLReader parser, SAXHandler contentHandler)
        throws JDOMException {

        // Setup SAX handlers.

        parser.setContentHandler(contentHandler);

        if (saxEntityResolver != null) {
            parser.setEntityResolver(saxEntityResolver);
        }

        if (saxDTDHandler != null) {
            parser.setDTDHandler(saxDTDHandler);
        } else {
            parser.setDTDHandler(contentHandler);
        }

        if (saxErrorHandler != null) {
             parser.setErrorHandler(saxErrorHandler);
        } else {
             parser.setErrorHandler(new BuilderErrorHandler());
        }

        // Set any user-specified features on the parser.
        Iterator iter = features.keySet().iterator();
        while(iter.hasNext()) {
            String name = (String)iter.next();
            Boolean value = (Boolean)features.get(name);
            internalSetFeature(parser, name, value.booleanValue(), name);
        }

        // Set any user-specified properties on the parser.
        Iterator iter2 = properties.keySet().iterator();
        while(iter2.hasNext()) {
            String name = (String)iter2.next();
            Object value = properties.get(name);
            internalSetProperty(parser, name, value, name);
        }

        // Setup lexical reporting.
        boolean lexicalReporting = false;
        try {
            parser.setProperty("http://xml.org/sax/handlers/LexicalHandler",
                               contentHandler);
            lexicalReporting = true;
        } catch (SAXNotSupportedException e) {
            // No lexical reporting available
        } catch (SAXNotRecognizedException e) {
            // No lexical reporting available
        }

        // Some parsers use alternate property for lexical handling (grr...)
        if (!lexicalReporting) {
            try {
                parser.setProperty(
                    "http://xml.org/sax/properties/lexical-handler",
                    contentHandler);
                lexicalReporting = true;
            } catch (SAXNotSupportedException e) {
                // No lexical reporting available
            } catch (SAXNotRecognizedException e) {
                // No lexical reporting available
            }
        }

        // Try setting the DeclHandler if entity expansion is off
        if (!expand) {
            try {
                parser.setProperty(
                    "http://xml.org/sax/properties/declaration-handler",
                    contentHandler);
            } catch (SAXNotSupportedException e) {
                // No lexical reporting available
            } catch (SAXNotRecognizedException e) {
                // No lexical reporting available
            }
        }

        // Set validation.
        try {
            internalSetFeature(parser, "http://xml.org/sax/features/validation", 
                    validate, "Validation");
        } catch (JDOMException e) {
            // If validation is not supported, and the user is requesting
            // that we don't validate, that's fine - don't throw an exception.
            if (validate)
                throw e;
        }

        // Setup some namespace features.
        internalSetFeature(parser, "http://xml.org/sax/features/namespaces", 
                true, "Namespaces");
        internalSetFeature(parser, "http://xml.org/sax/features/namespace-prefixes", 
                false, "Namespace prefixes");

        // Set entity expansion
        // Note SAXHandler can work regardless of how this is set, but when
        // entity expansion it's worth it to try to tell the parser not to
        // even bother with external general entities.
        // Apparently no parsers yet support this feature.
        // XXX It might make sense to setEntityResolver() with a resolver
        // that simply ignores external general entities
        try {
            if (parser.getFeature("http://xml.org/sax/features/external-general-entities") != expand) { 
                parser.setFeature("http://xml.org/sax/features/external-general-entities", expand);
            }

        }
        catch (SAXNotRecognizedException e) {
        /*
            // No entity expansion available
            throw new JDOMException(
              "Entity expansion feature not recognized by " + 
              parser.getClass().getName());
        */
        }
        catch (SAXNotSupportedException e) {
        /*
            // No entity expansion available
            throw new JDOMException(
              "Entity expansion feature not supported by " +
              parser.getClass().getName());
        */
        }
    }

    /**
     * <p>
     * Tries to set a feature on the parser. If the feature cannot be set,
     * throws a JDOMException describing the problem.
     * </p>
     */
    private void internalSetFeature(XMLReader parser, String feature, 
                    boolean value, String displayName) throws JDOMException {
        try {
            parser.setFeature(feature, value);
        } catch (SAXNotSupportedException e) {
            throw new JDOMException(
                displayName + " feature not supported for SAX driver " + parser.getClass().getName());
        } catch (SAXNotRecognizedException e) {
            throw new JDOMException(
                displayName + " feature not recognized for SAX driver " + parser.getClass().getName());
        }
    }

    /**
     * <p>
     * Tries to set a property on the parser. If the property cannot be set,
     * throws a JDOMException describing the problem.
     * </p>
     */
    private void internalSetProperty(XMLReader parser, String property, 
                    Object value, String displayName) throws JDOMException {
        try {
            parser.setProperty(property, value);
        } catch (SAXNotSupportedException e) {
            throw new JDOMException(
                displayName + " property not supported for SAX driver " + parser.getClass().getName());
        } catch (SAXNotRecognizedException e) {
            throw new JDOMException(
                displayName + " property not recognized for SAX driver " + parser.getClass().getName());
        }
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   input stream.
     * </p>
     *
     * @param in <code>InputStream</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(InputStream in) 
     throws JDOMException, IOException {
        return build(new InputSource(in));
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   filename.
     * </p>
     *
     * @param file <code>File</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(File file) 
        throws JDOMException, IOException {
        try {
            URL url = fileToURL(file);
            return build(url);
        } catch (MalformedURLException e) {
            throw new JDOMException("Error in building", e);
        }
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   URL.
     * </p>
     *
     * @param url <code>URL</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(URL url) 
        throws JDOMException, IOException {
        String systemID = url.toExternalForm();
        return build(new InputSource(systemID));
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   input stream.
     * </p>
     *
     * @param in <code>InputStream</code> to read from.
     * @param systemId base for resolving relative URIs
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(InputStream in, String systemId)
        throws JDOMException, IOException {

        InputSource src = new InputSource(in);
        src.setSystemId(systemId);
        return build(src);
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   Reader.
     * </p>
     *
     * @param in <code>Reader</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(Reader characterStream) 
        throws JDOMException, IOException {
        return build(new InputSource(characterStream));
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   Reader.
     * </p>
     *
     * @param in <code>Reader</code> to read from.
     * @param systemId base for resolving relative URIs
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(Reader characterStream, String SystemId)
        throws JDOMException, IOException {

        InputSource src = new InputSource(characterStream);
        src.setSystemId(SystemId);
        return build(src);
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   URI.
     * </p>
     * @param systemId URI for the input
     * @return <code>Document</code> - resultant Document object.
     * @throws JDOMException when errors occur in parsing.
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(String systemId) 
        throws JDOMException, IOException {
        return build(new InputSource(systemId));
    }

    /**
     * Imitation of File.toURL(), a JDK 1.2 method, reimplemented 
     * here to work with JDK 1.1.
     *
     * @see java.io.File
     *
     * @param f the file to convert
     * @return the file path converted to a file: URL
     */
    protected URL fileToURL(File f) throws MalformedURLException {
        String path = f.getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/") && f.isDirectory()) {
            path = path + "/";
        }
        return new URL("file", "", path);
    }

    /**
     * <p>
     * This sets whether or not to expand entities for the builder.
     * A true means to expand entities as normal content.  A false means to
     * leave entities unexpanded as <code>EntityRef</code> objects.  The 
     * default is true.
     * </p>
     * <p>
     * When this setting is false, the internal DTD subset is retained; when
     * this setting is true, the internal DTD subset is not retained.
     * </p>
     * <p>
     * Note that Xerces (at least up to 1.4.4) has a bug where entities
     * in attribute values will be misreported if this flag is turned off,
     * resulting in entities to appear within element content.  When turning
     * entity expansion off either avoid entities in attribute values, or
     * use another parser like Crimson.
     * http://nagoya.apache.org/bugzilla/show_bug.cgi?id=6111
     * </p>
     *
     * @param expand <code>boolean</code> indicating whether entity expansion 
     * should occur.
     */
    public void setExpandEntities(boolean expand) {
        this.expand = expand;
    }
}

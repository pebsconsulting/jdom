/*-- 

 $Id: IllegalDataException.java,v 1.8 2002/04/28 08:44:28 jhunter Exp $

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

package org.jdom;

/**
 * <p><code>IllegalDataException</code>
 *   is thrown when illegal text is supplied to a
 *   JDOM construct.
 * </p>
 *
 * @author Brett McLaughlin
 * @author Elliotte Rusty Harold
 * @version $Revision: 1.8 $, $Date: 2002/04/28 08:44:28 $
 */
public class IllegalDataException extends IllegalArgumentException {

    private static final String CVS_ID = 
      "@(#) $RCSfile: IllegalDataException.java,v $ $Revision: 1.8 $ $Date: 2002/04/28 08:44:28 $ $Name:  $";

    /**
     * <p>
     * This will create an <code>Exception</code> indicating
     *   that the specified data is illegal for the construct
     *   it was supplied to.
     * </p>
     *
     * @param data <code>String</code> data that breaks rules.
     * @param construct <code>String</code> construct that data is illegal for.
     * @param reason <code>String</code> message or reason data is illegal.
     */
    public IllegalDataException(String data, String construct, String reason) {
        super(new StringBuffer()
              .append("The data \"")
              .append(data)
              .append("\" is not legal for a JDOM ")
              .append(construct)
              .append(": ")
              .append(reason)
              .append(".")
              .toString());
    }

    /**
     * <p>
     * This will create an <code>Exception</code> indicating
     *   that the specified data is illegal for the construct
     *   it was supplied to.
     * </p>
     *
     * @param data <code>String</code> data that breaks rules.
     * @param construct <code>String</code> construct that data is illegal for.
     */
    public IllegalDataException(String data, String construct) {
        super(new StringBuffer()
              .append("The data \"")
              .append(data)
              .append("\" is not legal for a JDOM ")
              .append(construct)
              .append(".")
              .toString());
    }

}

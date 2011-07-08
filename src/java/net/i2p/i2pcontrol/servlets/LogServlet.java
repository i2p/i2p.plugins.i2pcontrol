package net.i2p.i2pcontrol.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.i2p.I2PAppContext;
import net.i2p.util.Log;
/*
 *  Copyright 2011 hottuna (dev@robertfoss.se)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
public class LogServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4018705582081424641L;
	private static Log _log;

	
	@Override
	public void init(){
		_log = I2PAppContext.getGlobalContext().logManager().getLog(LogServlet.class);
	}
	
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
    	httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        out.println("<html>\n<body>");
        out.println("<h2>Logs</h2>");
        out.println("<h3>Most recent: </h3>\r\n<br><pre>");
        List<String> strs = I2PAppContext.getGlobalContext().logManager().getBuffer().getMostRecentMessages();
        for (String s : strs){
        	out.println(s);
        }
        out.println("</pre>\r\n<br>\r\n<h3>Recent critical: </h3>\r\n<br><pre>");
        strs = I2PAppContext.getGlobalContext().logManager().getBuffer().getMostRecentCriticalMessages();
        for (String s : strs){
        	out.println(s);
        }
        out.println("\r\n<br></pre>\r\n</body>\n</html>");
        out.close();
    }

}

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
        out.println("<h3>Most recent: </h3>");
        List<String> strs = I2PAppContext.getGlobalContext().logManager().getBuffer().getMostRecentMessages();
        for (String s : strs){
        	out.println(s + "<br>");
        }
        out.println("<br>\r\n<h3>Recent critical: </h3>");
        strs = I2PAppContext.getGlobalContext().logManager().getBuffer().getMostRecentCriticalMessages();
        for (String s : strs){
        	out.println(s + "<br>");
        }
        out.println("</body>\n</html>");
        out.close();
    }

}

package net.i2p.i2pcontrol;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.i2p.I2PAppContext;
import net.i2p.util.Log;

public class HistoryServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4018705582081424641L;
	private static I2PControlManager _manager;
	private static Log _log;

	
	@Override
	public void init(){
		_log = I2PAppContext.getGlobalContext().logManager().getLog(HistoryServlet.class);
		_manager = I2PControlManager.getInstance();
	}
	
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
    	httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        out.println("<html>\n<body>");
        out.println(_manager.getHistory());
        out.println("</body>\n</html>");
        out.close();
    }

}

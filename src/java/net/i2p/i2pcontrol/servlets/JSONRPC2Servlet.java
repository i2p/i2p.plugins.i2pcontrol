package net.i2p.i2pcontrol.servlets;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.i2p.I2PAppContext;
import net.i2p.util.Log;

import net.i2p.i2pcontrol.I2PControlVersion;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.AuthenticateHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.EchoHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.GetRateHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.I2PControlHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.NetworkSettingHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.RouterInfoHandler;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.RouterManagerHandler;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;


/**
 * Provide an JSON-RPC 2.0 API for remote controlling of I2P
 */
public class JSONRPC2Servlet extends HttpServlet{

    private static final long serialVersionUID = -45075606818515212L;
    private static final int BUFFER_LENGTH = 2048;
    private static Dispatcher disp;
    private static char[] readBuffer;
    private static Log _log;


    @Override
    public void init(){
        _log = I2PAppContext.getGlobalContext().logManager().getLog(JSONRPC2Servlet.class);
        readBuffer = new char[BUFFER_LENGTH];

        disp = new Dispatcher();
        disp.register(new EchoHandler());
        disp.register(new GetRateHandler());
        disp.register(new AuthenticateHandler());
        disp.register(new NetworkSettingHandler());
        disp.register(new RouterInfoHandler());
        disp.register(new RouterManagerHandler());
        disp.register(new I2PControlHandler());
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException{
        httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        out.println("Nothing to see here");
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException{    
        String req = getRequest(httpServletRequest.getInputStream());
        httpServletResponse.setContentType("application/json");
        PrintWriter out = httpServletResponse.getWriter();
        JSONRPC2Message msg = null;
        JSONRPC2Response jsonResp = null;
        try {
            msg = JSONRPC2Message.parse(req);

            if (msg instanceof JSONRPC2Request) {
                jsonResp = disp.dispatch((JSONRPC2Request)msg, null);
                jsonResp.toJSON().put("API", I2PControlVersion.API_VERSION);
                _log.debug("Request: " + msg);
                _log.debug("Response: " + jsonResp);
            }
            else if (msg instanceof JSONRPC2Notification) {
                disp.dispatch((JSONRPC2Notification)msg, null);
                _log.debug("Notification: " + msg);
            }

            out.println(jsonResp);
            out.close();
        } catch (JSONRPC2ParseException e) {
            _log.error("Unable to parse JSONRPC2Message: " + e.getMessage());
        }
    }

    private String getRequest(ServletInputStream sis) throws IOException{
        Writer writer = new StringWriter();

        BufferedReader reader = new BufferedReader(new InputStreamReader(sis,"UTF-8"));
        int n;
        while ((n = reader.read(readBuffer)) != -1) {
            writer.write(readBuffer, 0, n);
        }
        return writer.toString();
    }
}

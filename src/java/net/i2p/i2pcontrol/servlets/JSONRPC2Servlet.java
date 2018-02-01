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

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import net.i2p.I2PAppContext;
import net.i2p.router.RouterContext;
import net.i2p.util.Log;

import net.i2p.i2pcontrol.I2PControlVersion;
import net.i2p.i2pcontrol.security.KeyStoreProvider;
import net.i2p.i2pcontrol.security.SecurityManager;
import net.i2p.i2pcontrol.servlets.jsonrpc2handlers.*;
import net.i2p.i2pcontrol.servlets.configuration.ConfigurationManager;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Provide an JSON-RPC 2.0 API for remote controlling of I2P
 */
public class JSONRPC2Servlet extends HttpServlet {

    private static final long serialVersionUID = -45075606818515212L;
    private static final int BUFFER_LENGTH = 2048;
    private Dispatcher disp;
    private Log _log;
    private final SecurityManager _secMan;
    private final JSONRPC2Helper _helper;
    private final RouterContext _context;

    /**
     *  Webapp
     */
    public JSONRPC2Servlet() {
        I2PAppContext ctx = I2PAppContext.getGlobalContext();
        if (!ctx.isRouterContext())
            throw new IllegalStateException();
        _context = (RouterContext) ctx;
        File appDir = ctx.getAppDir();
        String app = appDir.getAbsolutePath();
        ConfigurationManager conf = new ConfigurationManager(app);
        // we don't really need a keystore
        File ksDir = new File(ctx.getConfigDir(), "keystore");
        KeyStoreProvider ksp = new KeyStoreProvider(ksDir.getAbsolutePath());
        _secMan = new SecurityManager(ksp, conf);
        _helper = new JSONRPC2Helper(_secMan);
        _log = ctx.logManager().getLog(JSONRPC2Servlet.class);
    }

    /**
     *  Plugin
     */
    public JSONRPC2Servlet(RouterContext ctx, SecurityManager secMan) {
        _context = ctx;
        _secMan = secMan;
        _helper = new JSONRPC2Helper(_secMan);
        if (ctx != null)
            _log = ctx.logManager().getLog(JSONRPC2Servlet.class);
        else
            _log = I2PAppContext.getGlobalContext().logManager().getLog(JSONRPC2Servlet.class);
    }

    @Override
    public void init() {
        disp = new Dispatcher();
        disp.register(new EchoHandler(_helper));
        disp.register(new GetRateHandler(_helper));
        disp.register(new AuthenticateHandler(_helper, _secMan));
        disp.register(new NetworkSettingHandler(_context, _helper));
        disp.register(new RouterInfoHandler(_context, _helper));
        disp.register(new RouterManagerHandler(_context, _helper));
        disp.register(new I2PControlHandler(_context, _helper));
        disp.register(new AdvancedSettingsHandler(_context, _helper));
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setContentType("text/plain");
        PrintWriter out = httpServletResponse.getWriter();
        out.println("I2PControl RPC Service: Running");
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
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
                if (_log.shouldDebug()) {
                    _log.debug("Request: " + msg);
                    _log.debug("Response: " + jsonResp);
                }
            }
            else if (msg instanceof JSONRPC2Notification) {
                disp.dispatch((JSONRPC2Notification)msg, null);
                if (_log.shouldDebug())
                    _log.debug("Notification: " + msg);
            }

            out.println(jsonResp);
            out.close();
        } catch (JSONRPC2ParseException e) {
            _log.error("Unable to parse JSONRPC2Message: " + e.getMessage());
        }
    }

    private String getRequest(ServletInputStream sis) throws IOException {
        Writer writer = new StringWriter();

        BufferedReader reader = new BufferedReader(new InputStreamReader(sis, "UTF-8"));
        char[] readBuffer = new char[BUFFER_LENGTH];
        int n;
        while ((n = reader.read(readBuffer)) != -1) {
            writer.write(readBuffer, 0, n);
        }
        return writer.toString();
    }
}

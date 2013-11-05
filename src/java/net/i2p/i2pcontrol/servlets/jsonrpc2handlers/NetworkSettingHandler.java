package net.i2p.i2pcontrol.servlets.jsonrpc2handlers;

import java.util.HashMap;
import java.util.Map;

import net.i2p.I2PAppContext;
import net.i2p.data.RouterInfo;
import net.i2p.i2pcontrol.I2PControlController;
import net.i2p.i2pcontrol.router.RouterManager;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.router.transport.FIFOBandwidthRefiller;
import net.i2p.router.transport.TransportManager;
import net.i2p.router.transport.ntcp.NTCPTransport;
import net.i2p.router.transport.udp.UDPTransport;
import net.i2p.util.Log;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

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

public class NetworkSettingHandler implements RequestHandler {
    private static final int BW_BURST_PCT = 110;
    private static final int BW_BURST_TIME = 20;
    private static RouterContext _context;
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(NetworkSettingHandler.class);

    static{
        try {
            _context = RouterManager.getRouterContext();
        } catch (Exception e) {
            _log.error("Unable to initialize RouterContext.", e);
        }
    }

    // Reports the method names of the handled requests
    public String[] handledRequests() {
        return new String[]{"NetworkSetting"};
    }

    // Processes the requests
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
        if (req.getMethod().equals("NetworkSetting")) {
            return process(req);
        }else {
            // Method name not supported
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }


    private JSONRPC2Response process(JSONRPC2Request req){
        JSONRPC2Error err = JSONRPC2Helper.validateParams(null, req);
        if (err != null)
            return new JSONRPC2Response(err, req.getID());

        if (_context == null){
            return new JSONRPC2Response(
                    new JSONRPC2Error(JSONRPC2Error.INTERNAL_ERROR.getCode(), 
                            "RouterContext was not initialized. Query failed"), 
                            req.getID());
        }
        HashMap inParams = (HashMap) req.getParams();
        Map outParams = new HashMap();

        boolean restartNeeded = false;
        boolean settingsSaved = false;
        String inParam;

        if (inParams.containsKey("i2p.router.net.ntcp.port")){
            String oldNTCPPort = _context.getProperty(NTCPTransport.PROP_I2NP_NTCP_PORT);
            if ((inParam = (String) inParams.get("i2p.router.net.ntcp.port")) != null){
                if (oldNTCPPort == null || !oldNTCPPort.equals(inParam.trim())){
                    System.out.println("NTCP: " + oldNTCPPort + "->" + inParam);
                    _context.router().setConfigSetting(NTCPTransport.PROP_I2NP_NTCP_PORT, inParam);
                    _context.router().setConfigSetting(NTCPTransport.PROP_I2NP_NTCP_AUTO_PORT, "false"); // Duplicate below in setProperty to make sure it is properly set.
                    _context.setProperty(NTCPTransport.PROP_I2NP_NTCP_AUTO_PORT, "false"); //
                    restartNeeded = true;
                }
                settingsSaved = true;
            } else{
                String sAutoPort = _context.getProperty(NTCPTransport.PROP_I2NP_NTCP_AUTO_PORT, "true");
                boolean oldAutoPort = "true".equalsIgnoreCase(sAutoPort);
                if (oldAutoPort){
                    String oldSSUPort = "" + _context.getProperty(UDPTransport.PROP_INTERNAL_PORT, UDPTransport.DEFAULT_INTERNAL_PORT);
                    outParams.put("i2p.router.net.ntcp.port", oldSSUPort);
                } else {
                    outParams.put("i2p.router.net.ntcp.port", oldNTCPPort);
                }
            }
        }
        if(inParams.containsKey("i2p.router.net.ntcp.hostname")){
            String oldNTCPHostname = _context.getProperty(NTCPTransport.PROP_I2NP_NTCP_HOSTNAME);
            if ((inParam = (String) inParams.get("i2p.router.net.ntcp.hostname")) != null){
                if (oldNTCPHostname == null || !oldNTCPHostname.equals(inParam.trim())){
                    _context.router().setConfigSetting(NTCPTransport.PROP_I2NP_NTCP_HOSTNAME, inParam);
                    restartNeeded = true;
                }
                settingsSaved = true;
                } else {
                    outParams.put("i2p.router.net.ntcp.hostname", oldNTCPHostname);
            }
        }
        if(inParams.containsKey("i2p.router.net.ntcp.autoip")){
            String oldNTCPAutoIP = _context.getProperty(NTCPTransport.PROP_I2NP_NTCP_AUTO_IP);
            if ((inParam = (String) inParams.get("i2p.router.net.ntcp.autoip")) != null){
                inParam = inParam.trim().toLowerCase();
                if (oldNTCPAutoIP == null || !oldNTCPAutoIP.equals(inParam)){
                    if ("always".equals(inParam) || "true".equals(inParam) || "false".equals(inParam)){
                        _context.setProperty(NTCPTransport.PROP_I2NP_NTCP_AUTO_IP, inParam);
                        restartNeeded = true;
                    } else {
                        return new JSONRPC2Response(
                                new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
                                        "\"i2p.router.net.ntcp.autoip\" can only be always, true or false. " + inParam + " isn't valid."), 
                                        req.getID());
                    }
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.ntcp.autoip", oldNTCPAutoIP);
            }
        }
        if (inParams.containsKey("i2p.router.net.ssu.port")){
            String oldSSUPort = "" + _context.getProperty(UDPTransport.PROP_INTERNAL_PORT, UDPTransport.DEFAULT_INTERNAL_PORT);
            if ((inParam = (String) inParams.get("i2p.router.net.ssu.port")) != null){
                if (oldSSUPort== null || !oldSSUPort.equals(inParam.trim())){
                    System.out.println("UDP: " + oldSSUPort + "->" + inParam);
                    _context.router().setConfigSetting(UDPTransport.PROP_EXTERNAL_PORT, inParam);
                    _context.router().setConfigSetting(UDPTransport.PROP_INTERNAL_PORT, inParam);
                    restartNeeded = true;
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.ssu.port", oldSSUPort);
            }
        }
        if (inParams.containsKey("i2p.router.net.ssu.hostname")){
            String oldSSUHostname = _context.getProperty(UDPTransport.PROP_EXTERNAL_HOST);
            if ((inParam = (String) inParams.get("i2p.router.net.ssu.hostname")) != null){
                if (oldSSUHostname == null || !oldSSUHostname.equals(inParam.trim())){
                    _context.router().setConfigSetting(UDPTransport.PROP_EXTERNAL_HOST, inParam);
                    restartNeeded = true;
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.ssu.hostname", oldSSUHostname);
            }
        }
        if (inParams.containsKey("i2p.router.net.ssu.autoip")){
            String oldSSUAutoIP =  _context.getProperty(UDPTransport.PROP_SOURCES);
            if ((inParam = (String) inParams.get("i2p.router.net.ssu.autoip")) != null){
                inParam = inParam.trim().toLowerCase();
                if (oldSSUAutoIP == null || !oldSSUAutoIP.equals(inParam)){
                    if (inParam.equals("ssu") || inParam.equals("local,ssu") || inParam.equals("upnp,ssu") || inParam.equals("local,upnp,ssu")){
                    _context.router().setConfigSetting(UDPTransport.PROP_SOURCES, inParam);
                    restartNeeded = true;
                    } else {
                        return new JSONRPC2Response(
                                new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
                                        "\"i2p.router.net.ssu.autoip\" can only be ssu/local,upnp,ssu/local/ssu/upnp,ssu. " + inParam + " isn't valid."), 
                                        req.getID());
                    }
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.ssu.autoip", oldSSUAutoIP);
            }
        }
        // Non-setable key.
        if (inParams.containsKey("i2p.router.net.ssu.detectedip")){
            if ((inParam = (String) inParams.get("i2p.router.net.ssu.autoip")) == null){
                outParams.put("i2p.router.net.ssu.detectedip", _context.router().getRouterInfo().getTargetAddress("SSU"));
            }
        }
        if (inParams.containsKey("i2p.router.net.upnp")){
            String oldUPNP = _context.getProperty(TransportManager.PROP_ENABLE_UPNP);
            if ((inParam = (String) inParams.get("i2p.router.net.upnp")) != null){
                if (oldUPNP == null || !oldUPNP.equals(inParam.trim())){
                    _context.router().setConfigSetting(TransportManager.PROP_ENABLE_UPNP, inParam);
                    restartNeeded = true;
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.upnp", oldUPNP);
            }
        }
        if (inParams.containsKey("i2p.router.net.bw.share")){
            String oldShare = _context.router().getConfigSetting(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE);
            if ((inParam = (String) inParams.get("i2p.router.net.bw.share")) != null){
                if (oldShare == null || !oldShare.equals(inParam.trim())){
                    _context.router().setConfigSetting(Router.PROP_BANDWIDTH_SHARE_PERCENTAGE, inParam);
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.bw.share", oldShare);
            }
        }
        if (inParams.containsKey("i2p.router.net.bw.in")){
            String oldBWIn = _context.getProperty(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH);
            if ((inParam = (String) inParams.get("i2p.router.net.bw.in")) != null){
                Integer rate;
                try{
                    rate = Integer.parseInt(inParam);
                    if (rate < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e){
                    return new JSONRPC2Response(
                            new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
                                    "\"i2p.router.net.bw.in\" A positive integer must supplied, " + inParam + " isn't valid"), 
                                    req.getID());
                }
                Integer burstRate = (rate * BW_BURST_PCT)/100;
                Integer burstSize = (burstRate * BW_BURST_TIME);
                if (oldBWIn == null || !oldBWIn.equals(rate.toString())){
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH, rate.toString());
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_INBOUND_BURST_BANDWIDTH, burstRate.toString());
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_INBOUND_BANDWIDTH_PEAK, burstSize.toString());
                    _context.bandwidthLimiter().reinitialize();
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.bw.in", oldBWIn);
            }
        }
        if (inParams.containsKey("i2p.router.net.bw.out")){
            String oldBWOut = _context.getProperty(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH);
            if ((inParam = (String) inParams.get("i2p.router.net.bw.out")) != null){
                Integer rate;
                try{
                    rate = Integer.parseInt(inParam);
                    if (rate < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e){
                    return new JSONRPC2Response(
                            new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(), 
                                    "\"i2p.router.net.bw.out\" A positive integer must supplied, " + inParam + " isn't valid"), 
                                    req.getID());
                }
                Integer burstRate = (rate * BW_BURST_PCT)/100;
                Integer burstSize = (burstRate * BW_BURST_TIME);
                if (oldBWOut == null || !oldBWOut.equals(rate.toString())){
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH, rate.toString());
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_OUTBOUND_BURST_BANDWIDTH, burstRate.toString());
                    _context.router().setConfigSetting(FIFOBandwidthRefiller.PROP_OUTBOUND_BANDWIDTH_PEAK, burstSize.toString());
                    _context.bandwidthLimiter().reinitialize();
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.bw.out", oldBWOut);
            }
        }
        if (inParams.containsKey("i2p.router.net.laptopmode")){
            String oldLaptopMode = _context.getProperty(UDPTransport.PROP_LAPTOP_MODE);
            if ((inParam = (String) inParams.get("i2p.router.net.laptopmode")) != null){
                if (oldLaptopMode == null || !oldLaptopMode.equals(inParam.trim())){
                    _context.setProperty(UDPTransport.PROP_LAPTOP_MODE, inParam);
                }
                settingsSaved = true;
            } else {
                outParams.put("i2p.router.net.laptopmode", oldLaptopMode);
            }
        }

        if (settingsSaved)
            _context.router().saveConfig();

        outParams.put("SettingsSaved", settingsSaved);
        outParams.put("RestartNeeded", restartNeeded);
        return new JSONRPC2Response(outParams, req.getID());
    }
}

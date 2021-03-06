/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.server.communication;

import java.io.IOException;

import javax.servlet.ServletException;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.ServletPortletHelper;
import com.vaadin.server.SessionExpiredHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ApplicationConstants;

/**
 * Handles requests to open a push (bidirectional) communication channel between
 * the client and the server. After the initial request, communication through
 * the push channel is managed by {@link PushHandler}.
 * 
 * @author Vaadin Ltd
 * @since 7.1
 */
public class PushRequestHandler implements RequestHandler,
        SessionExpiredHandler {

    private AtmosphereFramework atmosphere;
    private PushHandler pushHandler;

    public PushRequestHandler(VaadinServletService service)
            throws ServiceException {

        atmosphere = new AtmosphereFramework();

        pushHandler = new PushHandler(service);
        atmosphere.addAtmosphereHandler("/*", pushHandler);
        atmosphere.addInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT,
                "true");

        final String bufferSize = String
                .valueOf(ApplicationConstants.WEBSOCKET_BUFFER_SIZE);
        atmosphere.addInitParameter(ApplicationConfig.WEBSOCKET_BUFFER_SIZE,
                bufferSize);
        atmosphere.addInitParameter(ApplicationConfig.WEBSOCKET_MAXTEXTSIZE,
                bufferSize);
        atmosphere.addInitParameter(ApplicationConfig.WEBSOCKET_MAXBINARYSIZE,
                bufferSize);

        // Disable Atmosphere's message about commercial support
        atmosphere.addInitParameter("org.atmosphere.cpr.showSupportMessage",
                "false");

        // Required to ensure the client-side knows at which points to split the
        // message stream into individual messages when using certain transports
        atmosphere.interceptor(new TrackMessageSizeInterceptor());

        try {
            atmosphere.init(service.getServlet().getServletConfig());
        } catch (ServletException e) {
            throw new ServiceException("Could not read atmosphere settings", e);
        }
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {

        if (!ServletPortletHelper.isPushRequest(request)) {
            return false;
        }

        if (request instanceof VaadinServletRequest) {
            try {
                atmosphere.doCometSupport(AtmosphereRequest
                        .wrap((VaadinServletRequest) request),
                        AtmosphereResponse
                                .wrap((VaadinServletResponse) response));
            } catch (ServletException e) {
                // TODO PUSH decide how to handle
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException(
                    "Portlets not currently supported");
        }

        return true;
    }

    public void destroy() {
        atmosphere.destroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.server.SessionExpiredHandler#handleSessionExpired(com.vaadin
     * .server.VaadinRequest, com.vaadin.server.VaadinResponse)
     */
    @Override
    public boolean handleSessionExpired(VaadinRequest request,
            VaadinResponse response) throws IOException {
        // Websockets request must be handled by accepting the websocket
        // connection and then sending session expired so we let
        // PushRequestHandler handle it
        return handleRequest(null, request, response);
    }
}

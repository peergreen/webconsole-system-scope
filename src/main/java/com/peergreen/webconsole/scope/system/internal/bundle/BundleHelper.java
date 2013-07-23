package com.peergreen.webconsole.scope.system.internal.bundle;

import org.osgi.framework.Bundle;

/**
 * User: guillaume
 * Date: 22/07/13
 * Time: 12:16
 */
public class BundleHelper {
    public static String getHeader(final Bundle bundle, final String name) {
        return bundle.getHeaders().get(name);
    }

    public static boolean isState(final Bundle bundle, final int flag) {
        return (bundle.getState() & flag) == flag;
    }


}

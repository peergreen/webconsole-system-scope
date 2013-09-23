/**
 * Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


    public static String getStateString(int state) {
        switch (state) {
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }
}

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

import static java.lang.String.format;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * User: guillaume
 * Date: 18/07/13
 * Time: 11:28
 */
public class BundleItem {
    private final Bundle bundle;

    public BundleItem(final Bundle bundle) {
        this.bundle = bundle;
    }

    public long getBundleId() {
        return bundle.getBundleId();
    }

    public Label getBundleName() {
        String name = BundleHelper.getHeader(bundle, Constants.BUNDLE_NAME);
        if (name != null) {
            return new Label(format("%s <i>(%s)</i>", name, bundle.getSymbolicName()),
                    ContentMode.HTML);
        }
        return new Label(bundle.getSymbolicName());
    }

    public String getVersion() {
        return bundle.getVersion().toString();
    }

    public int getState() {
        return bundle.getState();
    }

    public Bundle getBundle() {
        return bundle;
    }
}

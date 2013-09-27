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

package com.peergreen.webconsole.scope.system.internal;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Scope;
import com.peergreen.webconsole.navigator.Navigable;
import com.peergreen.webconsole.navigator.Navigate;
import com.peergreen.webconsole.navigator.NavigationContext;
import com.peergreen.webconsole.utils.UrlFragment;
import com.peergreen.webconsole.vaadin.tabs.TabScope;
import com.vaadin.ui.Component;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope")
@Navigable
@Scope(value = "system", iconClass = "icon-system")
public class SystemScope extends TabScope {

    public SystemScope() {
        super("System", true);
    }

    @Navigate
    public Component navigate(NavigationContext context) {
        Component tab = getComponents().get(UrlFragment.getFirstFragment(context.getPath()));
        context.setPath(UrlFragment.subFirstFragment(context.getPath()));
        if (tab != null) {
            setSelectedTab(tab);
        }
        return tab;
    }
}

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

package com.peergreen.webconsole.scope.system.internal.service;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * User: guillaume
 * Date: 17/07/13
 * Time: 11:32
 */
public class ServiceReferenceItem {
    private boolean opened = false;
    private final ServiceReference<?> reference;

    public ServiceReferenceItem(final ServiceReference<?> reference) {
        this.reference = reference;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(final boolean opened) {
        this.opened = opened;
    }

    public Long getServiceId() {
        return (Long) reference.getProperty(Constants.SERVICE_ID);
    }

    public String getBundleInfo() {
        Bundle bundle = reference.getBundle();
        return format("%s (%d)", bundle.getSymbolicName(), bundle.getBundleId());
    }

    public Component getInterfaces() {
        if (!opened) {
            return new Label(searchInterfaces());
        } else {
            GridLayout layout = new GridLayout(3, 1);

            for (String name : reference.getPropertyKeys()) {
                layout.addComponent(new Label(format("<b>%s</b>", name), ContentMode.HTML));
                layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
                layout.addComponent(new Label(convert(reference.getProperty(name)).toString(), ContentMode.PREFORMATTED));
                layout.newLine();
            }

            return layout;
        }
    }

    private Object convert(final Object value) {
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return Arrays.asList(array);
        }
        return value;
    }

    private String searchInterfaces() {
        List<String> interfaces = new ArrayList<>();
        String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        interfaces.addAll(Arrays.asList(classes));
        return interfaces.toString();
    }

    public Bundle getBundle() {
        return reference.getBundle();
    }

    public ServiceReference<?> getReference() {
        return reference;
    }
}

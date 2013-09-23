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

package com.peergreen.webconsole.scope.system.internal.service.filter;

import static java.lang.String.format;

import java.util.regex.Pattern;

import org.osgi.framework.Bundle;

import com.peergreen.webconsole.scope.system.internal.service.ServiceReferenceItem;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

/**
 * Perform Bundle filtering against SymbolicName and version (ignoring case).
 */
public abstract class AbstractServiceReferenceFilter implements Container.Filter {

    @Override
    public boolean passesFilter(final Object itemId, final Item item) throws UnsupportedOperationException {
        if (!(item instanceof BeanItem)) {
            throw new UnsupportedOperationException(format("Item %s is not a BeanItem", itemId));
        }
        BeanItem<ServiceReferenceItem> sri = (BeanItem<ServiceReferenceItem>) item;
        ServiceReferenceItem bean = sri.getBean();

        return doFilter(bean);
    }

    protected abstract boolean doFilter(final ServiceReferenceItem bean);

    @Override
    public boolean appliesToProperty(final Object propertyId) {
        return true;
    }
}

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

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.peergreen.webconsole.scope.system.internal.service.ServiceReferenceItem;

/**
 * Filter ServiceReferences using the objectClass service property.
 */
public class InterfacesFilter extends AbstractServiceReferenceFilter {

    private final Pattern pattern;

    public InterfacesFilter(final String pattern) {
        this(Pattern.compile(format(".*%s.*", pattern), Pattern.CASE_INSENSITIVE));
    }

    public InterfacesFilter(final Pattern pattern) {
        this.pattern = pattern;
    }

    protected boolean doFilter(final ServiceReferenceItem bean) {
        ServiceReference<?> reference = bean.getReference();

        String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        for (String type : classes) {
            if (pattern.matcher(type).matches()) {
                return true;
            }
        }

        return false;
    }
}

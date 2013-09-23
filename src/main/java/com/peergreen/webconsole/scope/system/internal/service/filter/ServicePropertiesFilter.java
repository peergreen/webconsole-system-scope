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

import org.osgi.framework.ServiceReference;

import com.peergreen.webconsole.scope.system.internal.service.ServiceReferenceItem;

/**
 * Filter ServiceReferences using the objectClass service property.
 */
public class ServicePropertiesFilter extends AbstractServiceReferenceFilter {

    private final Pattern pattern;

    public ServicePropertiesFilter(final String pattern) {
        this(Pattern.compile(format(".*%s.*", pattern), Pattern.CASE_INSENSITIVE));
    }

    public ServicePropertiesFilter(final Pattern pattern) {
        this.pattern = pattern;
    }

    protected boolean doFilter(final ServiceReferenceItem bean) {
        ServiceReference<?> reference = bean.getReference();

        for (String key : reference.getPropertyKeys()) {

            // Tries pattern matching against key name
            if (pattern.matcher(key).matches()) {
                return true;
            }

            // Then tries service properties values
            Object o = reference.getProperty(key);
            if (o != null) {
                String value = String.valueOf(o);
                if (pattern.matcher(value).matches()) {
                    return true;
                }
            }
        }

        return false;
    }
}

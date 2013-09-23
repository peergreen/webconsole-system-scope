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

/**
 * Perform Bundle filtering against SymbolicName and version (ignoring case).
 */
public class BundleFilter extends AbstractServiceReferenceFilter {

    private final Pattern pattern;

    public BundleFilter(final String pattern) {
        this(Pattern.compile(format(".*%s.*", pattern), Pattern.CASE_INSENSITIVE));
    }

    public BundleFilter(final Pattern pattern) {
        this.pattern = pattern;
    }

    protected boolean doFilter(final ServiceReferenceItem bean) {
        Bundle bundle = bean.getBundle();
        if (pattern.matcher(bundle.getSymbolicName()).matches()) {
            return true;
        }
        if (pattern.matcher(String.valueOf(bundle.getBundleId())).matches()) {
            return true;
        }
        return false;
    }
}

package dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IRowValueProvider;
import org.dbunit.dataset.filter.IRowFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for filtering columns with specific value from dataset.
 * @author vrouvine
 */
public class RowFilter implements IRowFilter {

    private static final Logger log = LoggerFactory.getLogger(RowFilter.class);
    private final String columnName;
    private final String filterValue;

    public RowFilter(String filterValue, String columnName) {
        this.filterValue = filterValue;
        this.columnName = columnName;
    }

    @Override
    public boolean accept(IRowValueProvider rowValueProvider) {
        Object columnValue;
        try {
            columnValue = rowValueProvider.getColumnValue(columnName);
        } catch (DataSetException ex) {
            log.error("Daset error: ", ex);
            return false;
        }
        if (((String) columnValue).equalsIgnoreCase(filterValue)) {
            return true;
        }
        return false;
    }
}

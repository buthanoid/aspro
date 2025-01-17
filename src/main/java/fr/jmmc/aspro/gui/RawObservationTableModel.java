/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.gui.action.QueryRawObservationsAction;
import fr.jmmc.aspro.model.rawobs.RawObservation;
import fr.jmmc.jmcs.model.ColumnDesc;
import fr.jmmc.jmcs.model.ColumnDescTableModel;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a specific table model (JTable) to display raw observations
 *
 * @author Laurent BOURGES
 */
public final class RawObservationTableModel extends ColumnDescTableModel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(RawObservationTableModel.class.getName());

    /** Column definition enum */
    private enum ColumnDef {
        GID("Group Id", Integer.class),
        ID("Id", String.class),
        TYPE("Type", String.class),
        PARENT_ID("Parent Id", String.class),
        PROGRAM_ID("Program", String.class),
        INTERF_NAME("Array", String.class),
        INTERF_VERSION("Period", String.class),
        STATIONS("Stations", String.class),
        POPS("PoPs", String.class),
        INS_NAME("Ins. Name", String.class),
        INS_MODE("Ins. Mode", String.class),
        INS_SUB_MODE("Ins. SubMode", String.class),
        TARGET_NAME("Target", String.class),
        TARGET_RA("RA", Double.class),
        TARGET_DEC("DEC", Double.class),
        MJD_START("MJD OBS", Double.class),
        TAU0("Tau0 (ms)", Double.class),
        TEMP("Temp (C)", Double.class),
        SEEING("Seeing (as)", Double.class),
        VALID("Valid", Integer.class),
        EXP_TIME("Exp. time", Double.class),
        TARGET_RA_HMS("RA (HMS)", String.class),
        TARGET_DEC_DMS("DEC (DMS)", String.class),
        DATE("Date (UTC)", String.class),
        TIME("Time (UTC)", String.class),
        LST_START("LST Start", Double.class);

        private final ColumnDesc columnDesc;

        private ColumnDef(String label, Class<?> dataClass) {
            this.columnDesc = new ColumnDesc(name(), dataClass, ColumnDesc.SOURCE_UNDEFINED, label);
        }

        public ColumnDesc getColumnDesc() {
            return columnDesc;
        }
    }

    /** empty collection */
    private static final List<RawObservation> EMPTY = Collections.emptyList();

    /* members */
    /** list of raw observations (row) present in the table */
    private List<RawObservation> observations = EMPTY;

    /**
     * Public constructor
     */
    public RawObservationTableModel() {
        super();
        // define fixed columns:
        for (ColumnDef c : ColumnDef.values()) {
            listColumnDesc.add(c.getColumnDesc());
        }
    }

    /**
     * Define the data to use in this table model
     *
     * @param observations
     */
    public void setData(final List<RawObservation> observations) {
        if (logger.isDebugEnabled()) {
            logger.debug("setData[{}]: {}", observations);
        }
        this.observations = (observations != null) ? observations : EMPTY;

        // fire the table data changed event :
        fireTableDataChanged();
    }

    public boolean hasURL(final int column) {
        if (column == ColumnDef.ID.ordinal()) {
            return true;
        }
        if (column == ColumnDef.PROGRAM_ID.ordinal()) {
            return true;
        }
        return false;
    }

    public String getURL(final int column, final int row) {
        final String id = (String) getValueAt(row, column);
        if (id != null) {
            if (column == ColumnDef.ID.ordinal()) {
                return QueryRawObservationsAction.OBS_SERVER_GET_OBS_URL + id;
            }
            if (column == ColumnDef.PROGRAM_ID.ordinal()) {
                return QueryRawObservationsAction.ESO_GET_PROG_URL + id;
            }
        }
        return null;
    }

    /**
     * Return the model corresponding to the row at
     * <code>rowIndex</code>
     *
     * @param	rowIndex	the row whose value is to be queried
     * @return model
     */
    public RawObservation getObsAt(final int rowIndex) {
        return this.observations.get(rowIndex);
    }

    /* TableModel interface implementation */
    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display. This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    @Override
    public int getRowCount() {
        return this.observations.size();
    }

    /**
     * Returns the value for the cell at
     * <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final RawObservation obs = getObsAt(rowIndex);
        final ColumnDesc columnDesc = getColumnDesc(columnIndex);

        switch (ColumnDef.valueOf(columnDesc.getName())) {
            case GID:
                return obs.getGroupId();
            case ID:
                return obs.getObsId();
            case TYPE:
                return obs.getType();
            case PARENT_ID:
                return obs.getParentId();
            case PROGRAM_ID:
                return obs.getProgramId();
            case INTERF_NAME:
                return obs.getInterferometerName();
            case INTERF_VERSION:
                return obs.getInterferometerVersion();
            case STATIONS:
                return obs.getStations();
            case POPS:
                return obs.getPops();
            case INS_NAME:
                return obs.getInstrumentName();
            case INS_MODE:
                return obs.getInstrumentMode();
            case INS_SUB_MODE:
                return obs.getInstrumentSubMode();
            case TARGET_NAME:
                return obs.getTargetName();
            case TARGET_RA:
                return obs.getTargetRa();
            case TARGET_DEC:
                return obs.getTargetDec();
            case MJD_START:
                return obs.getMjdStart();
            case TAU0:
                return (obs.getExpTau0() != null) ? 1000.0 * obs.getExpTau0() : null;
            case TEMP:
                return obs.getExpTemp();
            case SEEING:
                return obs.getExpSeeing();
            case VALID:
                return obs.getValid();
            case EXP_TIME:
                return obs.getExpTime();
            case TARGET_RA_HMS:
                return obs.getRa();
            case TARGET_DEC_DMS:
                return obs.getDec();
            case DATE:
                return obs.getDateStart();
            case TIME:
                return obs.getTimeStart();
            case LST_START:
                return obs.getLstStart();
            default:
        }
        return null;
    }

    /**
     * Returns true if the cell at
     * <code>rowIndex</code> and
     * <code>columnIndex</code>
     * is editable. Otherwise,
     * <code>setValueAt</code> on the cell will not
     * change the value of that cell.
     *
     * @param	rowIndex	the row whose value to be queried
     * @param	columnIndex	the column whose value to be queried
     * @return	true if the cell is editable
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return hasURL(columnIndex);
    }

    /**
     * Sets the value in the cell at
     * <code>columnIndex</code> and
     * <code>rowIndex</code> to
     * <code>aValue</code>.
     *
     * @param	aValue	the new value
     * @param	rowIndex	the row whose value is to be changed
     * @param	columnIndex the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // no-op
    }
}

package ar.com.leo.produccion.jdbc;

import ar.com.leo.produccion.model.Maquina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ar.com.leo.produccion.jdbc.DataSourceConfig.dataSource;

// * @author Leo
public class MaquinaDAO {

    public static List<Maquina> obtenerMaquinas() {

        List<Maquina> maquinas = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT MachCode, StyleCode, Pieces, TargetOrder, IdealCycle, State," +
                    " CASE WHEN (TargetOrder = 0) THEN" +
                    "   CASE WHEN (EXISTS(SELECT TOP 1 State FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5)) THEN" +
                    "       ((SELECT TOP 1 pm.TargetPieces FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 " +
                    "       ORDER BY DateRec DESC) + " +
                    "       ISNULL((SELECT SUM(pm.Pieces) FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND pm.DateRec > (SELECT TOP 1 DateRec FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 ORDER BY DateRec DESC)), 0) + MAX(m.LastPieces)) * 100 / (SELECT TOP 1 pm.TargetPieces FROM dbo.PRODUCTIONS_MONITOR pm WHERE pm.MachCode = m.MachCode AND pm.StyleCode = m.StyleCode AND MONTH(DateRec) = MONTH(Getdate()) AND YEAR(DateRec) = YEAR(Getdate()) AND Reason = 5 ORDER BY DateRec DESC)" +
                    "   ELSE" +
                    "       0" +
                    "   END" +
                    " ELSE" +
                    "   Pieces*100/TargetOrder" +
                    " END AS '%'" +
                    " FROM Machines AS m" +
                    " WHERE m.RoomCode = 'HOMBRE'" +
                    " GROUP BY MachCode, StyleCode, Pieces, TargetOrder, IdealCycle, State" +
                    " ORDER BY m.MachCode");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Maquina maquina = new Maquina();
                    maquina.setMachCode(rs.getInt("MachCode"));
                    maquina.setStyleCode(rs.getString("StyleCode").trim());
                    maquina.setPieces(rs.getInt("Pieces"));
                    maquina.setTargetOrder(rs.getInt("TargetOrder"));
                    maquina.setProduccion(rs.getInt("%"));
                    maquina.setIdealCycle(rs.getInt("IdealCycle"));
                    maquina.setState(rs.getInt("State"));
                    maquinas.add(maquina);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                //consider a re-throw, throwing a wrapping exception, etc
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maquinas;
    }

}

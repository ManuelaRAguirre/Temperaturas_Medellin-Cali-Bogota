
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import datechooser.beans.DateChooserCombo;
import operaciones.AcTemperaturas;
import operaciones.RegistrodeTemperaturas;

public class FrmInterfaz extends JFrame {

    private JComboBox<String> cmbCiudad;
    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpTemperaturas;
    private JPanel pnlGrafica, pnlEstadisticas;
    private List<String> ciudades;
    private List<RegistrodeTemperaturas> registros;

    public FrmInterfaz() {
        setTitle("Comparativa de temperaturas");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton("Gráfica");
        btnGraficar.setToolTipText("Graficar temperaturas por ciudad");
        btnGraficar.addActionListener(evt -> btnGraficarClick());
        tb.add(btnGraficar);

        JButton btnEstadisticas = new JButton("Estadísticas");
        btnEstadisticas.setToolTipText("Mostrar estadísticas por ciudad");
        btnEstadisticas.addActionListener(evt -> btnEstadisticasClick());
        tb.add(btnEstadisticas);

        JPanel pnlPrincipal = new JPanel();
        pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS));

        JPanel pnlFiltros = new JPanel(null);
        pnlFiltros.setPreferredSize(new Dimension(700, 50));

        JLabel lblCiudad = new JLabel("Ciudad:");
        lblCiudad.setBounds(10, 10, 100, 25);
        pnlFiltros.add(lblCiudad);

        cmbCiudad = new JComboBox<>();
        cmbCiudad.setBounds(80, 10, 150, 25);
        pnlFiltros.add(cmbCiudad);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(250, 10, 120, 25);
        pnlFiltros.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(390, 10, 120, 25);
        pnlFiltros.add(dccHasta);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();

        tpTemperaturas = new JTabbedPane();
        tpTemperaturas.addTab("Gráfica", spGrafica);
        tpTemperaturas.addTab("Estadísticas", pnlEstadisticas);

        pnlPrincipal.add(pnlFiltros);
        pnlPrincipal.add(tpTemperaturas);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlPrincipal, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        String nombreArchivo = System.getProperty("user.dir") + "/src/data/Temperaturas.csv";
        registros = AcTemperaturas.getDatos(nombreArchivo);
        ciudades = AcTemperaturas.getCiudades(registros);

        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>(ciudades.toArray(new String[0]));
        cmbCiudad.setModel(modelo);
    }

    private void btnGraficarClick() {
        if (cmbCiudad.getSelectedIndex() < 0)
            return;

        String ciudad = (String) cmbCiudad.getSelectedItem();
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        var promedios = AcTemperaturas.getPromedioPorCiudad(desde, hasta, registros);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (var entry : promedios.entrySet()) {
            dataset.addValue(entry.getValue(), "Promedio", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Promedio de temperaturas por ciudad",
                "Ciudad",
                "Temperatura (°C)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                true,
                false);

        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(600, 400));

        pnlGrafica.removeAll();
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(panel, BorderLayout.CENTER);
        pnlGrafica.revalidate();

        tpTemperaturas.setSelectedIndex(0);
    }

    private void btnEstadisticasClick() {
        if (cmbCiudad.getSelectedIndex() < 0)
            return;

        String ciudad = (String) cmbCiudad.getSelectedItem();
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Calendar limiteMax = Calendar.getInstance();
        limiteMax.set(2025, Calendar.NOVEMBER, 5);

        dccDesde.setMaxDate(limiteMax);
        dccHasta.setMaxDate(limiteMax);
        Calendar limiteMin = Calendar.getInstance();
        limiteMin.set(2010, Calendar.JANUARY, 1);
        dccDesde.setMinDate(limiteMin);
        dccHasta.setMinDate(limiteMin);

        pnlEstadisticas.removeAll();
        pnlEstadisticas.setLayout(new GridBagLayout());

        var filtrados = AcTemperaturas.filtrar(ciudad, desde, hasta, registros);

        double promedio = AcTemperaturas.getPromedio(filtrados);
        double max = AcTemperaturas.getMaximo(filtrados);
        double min = AcTemperaturas.getMinimo(filtrados);

        String[][] datos = {
                { "Promedio", String.format("%.2f °C", promedio) },
                { "Máximo", String.format("%.2f °C", max) },
                { "Mínimo", String.format("%.2f °C", min) }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        for (int i = 0; i < datos.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            pnlEstadisticas.add(new JLabel(datos[i][0] + ":"), gbc);
            gbc.gridx = 1;
            pnlEstadisticas.add(new JLabel(datos[i][1]), gbc);
        }

        tpTemperaturas.setSelectedIndex(1);
        pnlEstadisticas.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmInterfaz().setVisible(true));
    }
}

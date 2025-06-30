package edu.weijunyong.satedgesim.SimulationManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.StyleSheet;

import org.jfree.data.general.Series;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

import edu.weijunyong.satedgesim.ScenarioManager.simulationParameters;

public class ChartsGenerator {

	private List<String[]> records = new ArrayList<>();
	private String fileName;
	private XYChart chart;

	public ChartsGenerator(String fileName) {
		this.fileName = fileName;
		loadFile();

	}

	private void loadFile() {
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = file.readLine()) != null) {
				records.add(line.split(","));
			}
			file.close();
		} catch (Exception e) {
			SimLog.println("Problem reading file.");
		}
	}

	public double[] getColumn(String name) {
		double[] results = new double[records.size() - 1];
		int column = getColumnIndex(name);
		for (int line = 1; line < records.size(); line++) {
			if((records.get(line)[column]).compareTo("NaN") == 0) {
				results[line - 1] = 0;
			}
			else {
				results[line - 1] = Double.parseDouble(records.get(line)[column]);
			}
		}
		return results;
	}

	private int getColumnIndex(String name) {
		for (int j = 0; j < records.get(0).length; j++) {
			if (records.get(0)[j].trim().equals(name.trim())) {
				return j;
			}
		}
		return -1;
	}

	public void displayChart(String x_series, String y_series, String y_series_label, String folder) {
		byAlgorithms(x_series, y_series, y_series_label, folder);
		byArchitectures(x_series, y_series, y_series_label, folder);
	}

	private void byAlgorithms(String x_series, String y_series, String y_series_label, String folder) {
		for (int orch = 0; orch < simulationParameters.ORCHESTRATION_ARCHITECTURES.length; orch++) {
			chart = new XYChartBuilder().height(400).width(600).theme(ChartTheme.Matlab).xAxisTitle(x_series)
					.yAxisTitle(y_series_label).build();
			chart.setTitle(y_series + " (" + simulationParameters.ORCHESTRATION_ARCHITECTURES[orch] + ")");
			chart.getStyler().setLegendVisible(true);
			
				        
			for (int alg = 0; alg < simulationParameters.ORCHESTRATION_AlGORITHMS.length; alg++) {
				double[] xData = toArray(getColumn(x_series, simulationParameters.ORCHESTRATION_ARCHITECTURES[orch],
						simulationParameters.ORCHESTRATION_AlGORITHMS[alg]));
				double[] yData = toArray(getColumn(y_series, simulationParameters.ORCHESTRATION_ARCHITECTURES[orch],
						simulationParameters.ORCHESTRATION_AlGORITHMS[alg]));
				
				XYSeries series = chart.addSeries(simulationParameters.ORCHESTRATION_AlGORITHMS[alg], xData, yData);
				
				//added
				// Use a switch statement to set the marker shape based on the algorithm
				series.setLineWidth(2);
				switch (simulationParameters.ORCHESTRATION_AlGORITHMS[alg]) {
	                case "ROUND_ROBIN":
	                    series.setMarker(SeriesMarkers.CROSS);
	                    series.setMarkerColor(Color.RED);
                		series.setLineColor(Color.RED);
	                    break;
	                case "TRADE_OFF":
	                    series.setMarker(SeriesMarkers.TRIANGLE_DOWN);
	                    series.setMarkerColor(Color.GRAY);
	                    series.setLineColor(Color.GRAY);
	                    break;
	                case "TRADI_POLLING":
	                    series.setMarker(SeriesMarkers.PLUS);
	                    series.setMarkerColor(Color.MAGENTA);
	                    series.setLineColor(Color.MAGENTA);
	                    break;
	                case "WG":
	                    series.setMarker(SeriesMarkers.CIRCLE);
	                    series.setMarkerColor(Color.BLUE);
	                    series.setLineColor(Color.BLUE);
	                    break;
	                case "WEIGHT_GREEDY":
	                    series.setMarker(SeriesMarkers.CIRCLE);
	                    series.setMarkerColor(Color.BLUE);
	                    series.setLineColor(Color.BLUE);
	                    break;
	                case "WG_D":
	                    series.setMarker(SeriesMarkers.DIAMOND);
	                    series.setMarkerColor(Color.GREEN);
	                    series.setLineColor(Color.GREEN);
	                    break;
	                case "WG_X":
	                    series.setMarker(SeriesMarkers.CROSS);
	                    series.setMarkerColor(Color.RED);
	                    series.setLineColor(Color.RED);
	                    break;
	                case "WG_V":
	                    series.setMarker(SeriesMarkers.PLUS);
	                    series.setMarkerColor(Color.CYAN);
	                    series.setLineColor(Color.CYAN);
	                    break;
	                case "WG_E":
	                    series.setMarker(SeriesMarkers.SQUARE);
	                    series.setMarkerColor(Color.BLACK);
	                    series.setLineColor(Color.BLACK);
	    				//series.setLineStyle(SeriesLines.DASH_DASH);
                        //series.setLineStyle(BasicStroke.CAP_ROUND);
	                    break;
	                case "RANDOM_VM":
	                    series.setMarker(SeriesMarkers.SQUARE);
	                    series.setMarkerColor(Color.PINK);
	                    series.setLineColor(Color.PINK);
	                    break;
	                case "D_ONLY":
	                    series.setMarker(SeriesMarkers.DIAMOND);
	                    series.setMarkerColor(Color.GREEN);
	                    series.setLineColor(Color.GREEN);
	                    break;
	                case "K_1":
	                    series.setMarker(SeriesMarkers.DIAMOND);
	                    series.setMarkerColor(Color.GREEN);
	                    series.setLineColor(Color.GREEN);
	                    break;
	                case "K_10":
	                	//series.setMarker(SeriesMarkers.PLUS);
	                    //series.setMarkerColor(Color.CYAN);
	                    //series.setLineColor(Color.CYAN);
	                    series.setMarker(SeriesMarkers.DIAMOND);
	                    series.setMarkerColor(Color.GREEN);
	                    series.setLineColor(Color.GREEN);
	                    break;
	                case "K_20":
	                    series.setMarker(SeriesMarkers.CROSS);
	                    series.setMarkerColor(Color.RED);
	                    series.setLineColor(Color.RED);
	                    break;
//	                case "APPOLO":
	                case "ISOLINK":
	                // Add more cases for other algorithms and marker shapes as needed
	                    series.setMarker(SeriesMarkers.DIAMOND);
	                    series.setMarkerColor(Color.BLACK);
	                    series.setLineColor(Color.BLACK);
	                    break;
	                case "K_50":
	                    series.setMarker(SeriesMarkers.CROSS);
	                    series.setMarkerColor(Color.RED);
	                    series.setLineColor(Color.RED);
	                // Add more cases for other algorithms and marker shapes as needed
	                    break;
	                case "K_70":
	                    series.setMarker(SeriesMarkers.PLUS);
	                    series.setMarkerColor(Color.BLACK);
	                    series.setLineColor(Color.BLACK);
	                // Add more cases for other algorithms and marker shapes as needed
	                    break;
	                default:
	                    series.setMarker(SeriesMarkers.CIRCLE); // Use a default marker if no case matches
	                    break;
	            }
	            
	            
				
				//orig//series.setMarker(SeriesMarkers.CIRCLE); // Marker type :circle,rectangle, diamond..
				series.setLineStyle(new BasicStroke());
			}
			// Save the chart
			saveBitmap("Algorithms" + folder + "/",
					y_series + "__" + simulationParameters.ORCHESTRATION_ARCHITECTURES[orch]);
		}
	}

	public void byArchitectures(String x_series, String y_series, String y_series_label, String folder) {
		for (int alg = 0; alg < simulationParameters.ORCHESTRATION_AlGORITHMS.length; alg++) {
			chart = new XYChartBuilder().height(400).width(600).theme(ChartTheme.Matlab).xAxisTitle(x_series)
					.yAxisTitle(y_series_label).build();
			chart.setTitle(y_series + " (" + simulationParameters.ORCHESTRATION_AlGORITHMS[alg] + ")");
			chart.getStyler().setLegendVisible(true);
			for (int orch = 0; orch < simulationParameters.ORCHESTRATION_ARCHITECTURES.length; orch++) {
				double[] xData = toArray(getColumn(x_series, simulationParameters.ORCHESTRATION_ARCHITECTURES[orch],
						simulationParameters.ORCHESTRATION_AlGORITHMS[alg]));
				double[] yData = toArray(getColumn(y_series, simulationParameters.ORCHESTRATION_ARCHITECTURES[orch],
						simulationParameters.ORCHESTRATION_AlGORITHMS[alg]));
				XYSeries series = chart.addSeries(simulationParameters.ORCHESTRATION_ARCHITECTURES[orch], xData, yData);
				series.setMarker(SeriesMarkers.CIRCLE); // Marker type :circle,rectangle, diamond..
				series.setLineStyle(new BasicStroke());
			}
			// Save the chart
			saveBitmap("Architectures" + folder + "/",
					y_series + "__" + simulationParameters.ORCHESTRATION_AlGORITHMS[alg]);
		}
	}

	private void saveBitmap(String folder, String name) {
		try {
			File file = new File(new File(fileName).getParent() + "/Final results/" + folder);
			file.mkdirs();
			BitmapEncoder.saveBitmapWithDPI(chart, file.getPath() + "/" + name.replace("/", " per "), BitmapFormat.PNG,
					300);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private List<Double> getColumn(String name, String orch, String alg) {
		List<Double> list = new ArrayList<>();
		int column = getColumnIndex(name);
		for (int line = 1; line < records.size(); line++) {
			if (records.get(line)[0].trim().equals(orch.trim()) && records.get(line)[1].trim().equals(alg.trim())) {
				if((records.get(line)[column]).compareTo("NaN") == 0) {
					list.add(0.0);
				}
				else {
					list.add(Double.parseDouble(records.get(line)[column]));
				}
			}
		}
		return list;
	}

	private double[] toArray(List<Double> list) {
		double[] results = new double[list.size()];
		for (int i = 0; i < list.size(); i++)
			results[i] = list.get(i);
		return results;
	}

	public void generate() {
		generateTasksCharts();
		generateNetworkCharts();
		generateCpuCharts();
		generateEnergyCharts(); 
	}

	private void generateEnergyCharts() {
		displayChart("Edge devices count", "Energy consumption (dBW)", "Consumed energy (dBW)", "/Energy");
		displayChart("Edge devices count", "Average energy consumption (dBW/Data center)", "Consumed energy (dBW)",
				"/Energy");
		displayChart("Edge devices count", "Average energy consumption (dBW/Task)", "Consumed energy (dBW)",
				"/Energy");
		displayChart("Edge devices count", "Cloud energy consumption (dBW)", "Consumed energy (dBW)", "/Energy");
		displayChart("Edge devices count", "Average Cloud energy consumption (dBW/Data center)", "Consumed energy (dBW)",
				"/Energy");
		displayChart("Edge devices count", "Edge energy consumption (dBW)", "Consumed energy (dBW)", "/Energy");
		displayChart("Edge devices count", "Average Edge energy consumption (dBW/Data center)", "Consumed energy (dBW)",
				"/Energy");
		displayChart("Edge devices count", "Mist energy consumption (dBW)", "Consumed energy (dBW)", "/Energy");
		displayChart("Edge devices count", "Average Mist energy consumption (dBW/Device)", "Consumed energy (dBW)",
				"/Energy");
		
		displayChart("Edge devices count", "Energy consumption (W)", "Consumed energy (W)", "/Energy");
		displayChart("Edge devices count", "Average energy consumption (W/Data center)", "Consumed energy (W)",
				"/Energy");
		displayChart("Edge devices count", "Average energy consumption (W/Task)", "Consumed energy (W)",
				"/Energy");
		displayChart("Edge devices count", "Cloud energy consumption (W)", "Consumed energy (W)", "/Energy");
		displayChart("Edge devices count", "Average Cloud energy consumption (W/Data center)", "Consumed energy (W)",
				"/Energy");
		displayChart("Edge devices count", "Edge energy consumption (W)", "Consumed energy (W)", "/Energy");
		displayChart("Edge devices count", "Average Edge energy consumption (W/Data center)", "Consumed energy (W)",
				"/Energy");
		displayChart("Edge devices count", "Mist energy consumption (W)", "Consumed energy (W)", "/Energy");
		displayChart("Edge devices count", "Average Mist energy consumption (W/Device)", "Consumed energy (W)",
				"/Energy");

		displayChart("Edge devices count", "Dead devices count", "Count", "/Edge Devices");
		displayChart("Edge devices count", "Average remaining power (Wh)", "Remaining energy (Wh)", "/Edge Devices");
		displayChart("Edge devices count", "Average remaining power (%)", "Remaining energy (%)", "/Edge Devices");
		displayChart("Edge devices count", "First edge device death time (s)", "Time (s)", "/Edge Devices");

	}

	private void generateCpuCharts() {
		displayChart("Edge devices count", "Average VM CPU usage (%)", "CPU utilization (%)", "/CPU Utilization");
		displayChart("Edge devices count", "Average VM CPU usage (Cloud) (%)", "CPU utilization (%)",
				"/CPU Utilization");
		displayChart("Edge devices count", "Average VM CPU usage (Edge) (%)", "CPU utilization (%)", "/CPU Utilization");
		displayChart("Edge devices count", "Average VM CPU usage (Mist) (%)", "CPU utilization (%)",
				"/CPU Utilization");
	}

	private void generateNetworkCharts() {
		displayChart("Edge devices count", "Network usage (s)", "Time (s)", "/Network");
		displayChart("Edge devices count", "Wan usage (s)", "Time (s)", "/Network");
		displayChart("Edge devices count", "Average bandwidth per task (Mbps)", "Bandwidth (Mbps)", "/Network");
		if (simulationParameters.ENABLE_REGISTRY) {
			displayChart("Edge devices count", "Containers wan usage (s)", "Time (s)", "/Network");
			displayChart("Edge devices count", "Containers lan usage (s)", "Time (s)", "/Network");
		}
	}

	private void generateTasksCharts() {
		displayChart("Edge devices count", "Average ETE delay (s)", "Time (s)", "/Delays");
		displayChart("Edge devices count", "Average wainting time (s)", "Time (s)", "/Delays");
		displayChart("Edge devices count", "Average execution delay (s)", "Time (s)", "/Delays");

		displayChart("Edge devices count", "Tasks successfully executed", "Number of tasks", "/Tasks");
		
		displayChart("Edge devices count", "Tasks success rate(%)", "Success rate (%)", "/Tasks");
		displayChart("Edge devices count", "Tasks failed rate(delay)(%)", "failed rate (%)", "/Tasks");
		displayChart("Edge devices count", "Tasks failed rate(device dead)(%)", "failed rate (%)", "/Tasks");
		displayChart("Edge devices count", "Tasks failed rate(mobility)(%)", "failed rate (%)", "/Tasks");
		
		displayChart("Edge devices count", "Tasks failed (delay)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks failed (device dead)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks failed (mobility)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks not generated due to the death of devices", "Number of tasks",
				"/Tasks");

		displayChart("Edge devices count", "Total tasks executed (Cloud)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks successfully executed (Cloud)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Total tasks executed (Edge)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks successfully executed (Edge)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Total tasks executed (Mist)", "Number of tasks", "/Tasks");
		displayChart("Edge devices count", "Tasks successfully executed (Mist)", "Number of tasks", "/Tasks");

	}

}

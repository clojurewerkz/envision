dimple.plot.linearTrend = {

  // By default the bar series is stacked if there are series categories
  stacked: false,

  // This is not a grouped plot meaning that one point is treated as one series value
  grouped: false,

  supportedAxes: ["x", "y"],

  // Draw the chart
  draw: function (chart, series, duration) {

    var line = d3.svg.line()
          .x(function (d) {return dimple._helpers.cx(d, chart, chart.series[0]); })
          .y(function (d) { return dimple._helpers.cy(d, chart, chart.series[0]); });

    var path = svg_
          .append("g")
          .append("path");

    path.attr("d", line([{"cx":116,"cy":193.84640752178078},
                         {"cx":0,"cy":137.08773938580373}]))
      .style("stroke", "blue");


  }
};

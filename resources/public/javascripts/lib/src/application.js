/** @jsx React.DOM */

var HistogramChart = React.createClass({
  render: function() {
    return <div></div>;
  },

  getInitialState: function() {
    return {data: renderData};
  },

  componentDidMount: function() {
    this.svg = dimple.newSvg(this.getDOMNode(), this.props.width, this.props.height);
    this.chart = new dimple.chart(this.svg, this.state.data);
    this.chart.setBounds(50, 50, this.props.width - 50, this.props.height - 120);

    this.xAxis = this.chart.addCategoryAxis("x", this.props.x);
    this.yAxis = this.chart.addMeasureAxis("y", this.props.y);
    this.yAxis.addOrderRule("x");



    this.chart.addSeries([this.props.x, this.props.y], dimple.plot.bar);
    this.chart.draw();


    this.xAxis.titleShape.text(this.props.xTitle);
    this.yAxis.titleShape.text("Frequency");

  },

  shouldComponentUpdate: function(props) {
    this.chart.data = this.state.data;
    this.chart.draw();
    return false;
  }
});

React.renderComponent(<HistogramChart width={1140} height={320} x={"x"} y={["y"]} />, document.getElementById('example'));

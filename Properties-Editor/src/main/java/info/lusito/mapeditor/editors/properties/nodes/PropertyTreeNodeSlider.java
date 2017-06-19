package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.converters.ConvertUtil;
import info.lusito.mapeditor.utils.DialogUtil;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;

public class PropertyTreeNodeSlider extends PropertyTreeNode {

    protected final Slider slider = new Slider();
    private final Tooltip tooltip;
    private boolean isOver;

    public PropertyTreeNodeSlider(PropertyInterface property) {
        super(property);

        tooltip = new Tooltip();
        tooltip.setAutoFix(false);
        tooltip.setAutoHide(false);
        tooltip.setHideOnEscape(false);
        tooltip.setTextAlignment(TextAlignment.CENTER);
        tooltip.textProperty().bind(slider.valueProperty().asString("%.2f").concat("\nRight click to manually change"));
        slider.valueProperty().addListener((ov, o, n) -> applyValue(Double.toString(slider.getValue())));
        slider.setOnContextMenuRequested((e) -> showEditDialog());
        slider.setOnMouseMoved(this::positionTooltip);
        slider.setOnMouseDragged((e)-> {
            if(isOver)
                positionTooltip(e);
        });
        slider.setOnMouseEntered((e) -> isOver = true);
        slider.setOnMouseExited((e) -> {
            tooltip.hide();
            isOver = false;
        });
        slider.setOnDragExited((e) -> {
            if(!isOver)
                tooltip.hide();
        });
    }
    
    private void positionTooltip(MouseEvent e) {
        final double w = tooltip.getWidth();
        final double x = e.getScreenX() - w / 2;
        final double y = e.getScreenY() + 20;
        if (!tooltip.isShowing()) {
            tooltip.show(slider, x, y);
        } else {
            tooltip.setAnchorX(x);
            tooltip.setAnchorY(y);
        }
    }

    private void showEditDialog() {
        String val = DialogUtil.prompt("Change the value manually", getName(), slider.valueProperty().asString().get());
        if (val != null) {
            Double f = ConvertUtil.toDouble(val);
            if (f != null) {
                slider.setValue(f);
            } else {
                DialogUtil.message("Invalid value.", DialogUtil.Icon.ERROR);
            }
        }
    }

    public void addToCell(PropertyTreeTableCell cell) {
        String minValue = property.getMinimum();
        String maxValue = property.getMaximum();
        Double min = ConvertUtil.toDouble(minValue);
        Double max = ConvertUtil.toDouble(maxValue);
        Double value = ConvertUtil.toDouble(getValue());
        slider.setMin(min == null ? 0 : min);
        slider.setMax(max == null ? 100 : max);
        disableApplyValue = true;
        slider.setValue(value == null ? slider.getMin() : value);
        disableApplyValue = false;
        double diff = slider.getMax() - slider.getMin();
        slider.setMajorTickUnit(diff / 2);
        setValue(getValue(), false);

        cell.setText(null);
        cell.setControlGraphic(slider);
        cell.addFocusSelect(slider);
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            setValue(getDefaultValue());
            slider.setDisable(true);
        } else {
            setValue(value);
            slider.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }
    
    public void setValue(String value) {
        final Double d = ConvertUtil.toDouble(value);
        slider.setValue(d == null ? 0 : d);
    }

    @Override
    public void focusControl() {
        slider.requestFocus();
    }
}

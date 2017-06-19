package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import info.lusito.mapeditor.persistence.shape.EcoPolygon;
import info.lusito.mapeditor.persistence.shape.EcoPolyline;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;

public class ShapeConverter implements Converter {

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        if(value instanceof EcoCircle) {
            EcoCircle circle = (EcoCircle)value;
            writer.addAttribute("type", "circle");
            writer.setValue(circle.toData());
        } else if(value instanceof EcoRectangle) {
            EcoRectangle rect = (EcoRectangle)value;
            writer.addAttribute("type", "rectangle");
            writer.setValue(rect.toData());
        } else if(value instanceof EcoPolygon) {
            EcoPolygon polygon = (EcoPolygon)value;
            writer.addAttribute("type", "polygon");
            writer.setValue(polygon.toData());
        } else if(value instanceof EcoPolyline) {
            EcoPolyline polyline = (EcoPolyline)value;
            writer.addAttribute("type", "polyline");
            writer.setValue(polyline.toData());
        } else {
            //Fixme: error
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String type = reader.getAttribute("type");
        if(type != null) {
            switch(type) {
                case "circle":
                    return new EcoCircle(reader.getValue());
                case "rectangle":
                    return new EcoRectangle(reader.getValue());
                case "polygon":
                    return new EcoPolygon(reader.getValue());
                case "polyline":
                    return new EcoPolyline(reader.getValue());
                default:
                    //Fixme: error
                    break;
            }
        }
        return null;
    }

    public boolean canConvert(Class clazz) {
        return EcoShape.class.isAssignableFrom(clazz);
    }

}

package info.lusito.mapeditor.persistence.shape;

import info.lusito.mapeditor.persistence.utils.ParseOrDefault;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ShapeUtil {

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    public static float dot(EcoPoint fromA, EcoPoint toA, EcoPoint fromB, EcoPoint toB) {
        return dot(toA.x - fromA.x, toA.y - fromA.y, toB.x - fromB.x, toB.y - fromB.y);
    }

    public static void findBoundsCenter(EcoPoint dest, List<EcoPoint> points) {
        EcoPoint first = points.get(0);
        float minX = first.x;
        float maxX = first.x;
        float minY = first.y;
        float maxY = first.y;
        for (EcoPoint point : points) {
            if(point.x > maxX)
                maxX = point.x;
            else if(point.x < minX)
                minX = point.x;
            if(point.y > maxY)
                maxY = point.y;
            else if(point.y < minY)
                minY = point.y;
        }
        dest.x = minX + (maxX - minX)/2;
        dest.y = minY + (maxY - minY)/2;
    }

    public static List<EcoPoint> copyPointList(List<EcoPoint> other) {
        List<EcoPoint> points = new ArrayList(other.size());
        for (EcoPoint point : other) {
            points.add(new EcoPoint(point));
        }
        return points;
    }

    static void updateBounds(EcoCircle bounds, List<EcoPoint> points) {
        ShapeUtil.findBoundsCenter(bounds.center, points);
        bounds.radius = 0;
        for (EcoPoint point : points) {
            float dist = point.dist(bounds.center);
            if (dist > bounds.radius) {
                bounds.radius = dist;
            }
        }
    }

    static boolean linesIntersect(EcoPoint a1, EcoPoint a2, EcoPoint b1, EcoPoint b2) {
        return Line2D.linesIntersect(a1.x, a1.y, a2.x, a2.y, b1.x, b1.y, b2.x, b2.y);
    }

    static float pointLineDistance(EcoPoint a1, EcoPoint a2, EcoPoint pt) {
        return (float) Line2D.ptLineDist(a1.x, a1.y, a2.x, a2.y, pt.x, pt.y);
    }

    static String pointListDoData(List<EcoPoint> points) {
        StringJoiner sj = new StringJoiner(",");
        for (EcoPoint point : points) {
            sj.add(Float.toString(point.x)).add(Float.toString(point.y));
        }
        return sj.toString();
    }

    static List<EcoPoint> dataToPointsList(String data) {
        List<EcoPoint> list = new ArrayList();
        if (data != null) {
            String[] split = data.split(",");
            assert (split.length % 2 == 0);
            for (int i = 0; i < split.length; i += 2) {
                list.add(new EcoPoint(
                        ParseOrDefault.getFloat(split[i], 0),
                        ParseOrDefault.getFloat(split[i + 1], 0)
                ));
            }
        }
        return list;
    }
}

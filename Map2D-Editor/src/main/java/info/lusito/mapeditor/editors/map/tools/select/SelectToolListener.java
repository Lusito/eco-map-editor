package info.lusito.mapeditor.editors.map.tools.select;

import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;

public interface SelectToolListener {

    boolean hasSelection();
    void clearSelection();
    boolean testSelection(EcoCircle circle);
    void touch(EcoCircle circle);
    boolean setMoveSelection(EcoCircle circle);
    void addSelection(BoundingRect rect);
    void removeSelection(BoundingRect rect);
    void toggleSelection(BoundingRect rect);
    void invertSelection();
    void selectAll();
    void deleteSelection();

    void moveStart();
    void moveUpdate(float x, float y); // x,y = world drag delta
    void moveFinish();
    void moveCancel();

    void rotateStart(float x, float y); // x,y = world mouse position
    void rotateUpdate(float x, float y); // x,y = world mouse position
    void rotateFinish();
    void rotateCancel();

    void scaleStart(float x, float y); // x,y = world mouse position
    void scaleUpdate(float x, float y); // x,y = world mouse position
    void scaleFinish();
    void scaleCancel();
}

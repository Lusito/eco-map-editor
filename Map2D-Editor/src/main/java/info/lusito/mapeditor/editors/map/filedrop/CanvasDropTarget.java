package info.lusito.mapeditor.editors.map.filedrop;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CanvasDropTarget implements DropTargetListener {

    private final LwjglAWTCanvas awtCanvas;
    private final EcoMapFX mapFX;
    private final ArrayList<FileDropListener> listeners = new ArrayList();
    private final Vector2 worldPos = new Vector2();
    private final SimpleCamera camera;

    public CanvasDropTarget(LwjglAWTCanvas awtCanvas, EcoMapFX mapFX, SimpleCamera camera) {
        this.awtCanvas = awtCanvas;
        this.mapFX = mapFX;
        DropTarget dropTarget = new DropTarget(awtCanvas.getCanvas(), DnDConstants.ACTION_LINK, this, true);
        listeners.add(new ImageFileDrop(mapFX));
        listeners.add(new EntityFileDrop(mapFX));
        listeners.add(new TilesetFileDrop(mapFX));
        this.camera = camera;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (isFileList(dtde.getTransferable())) {
            dtde.acceptDrag(DnDConstants.ACTION_LINK);
            awtCanvas.setCursor(DragSource.DefaultLinkDrop);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (isFileList(dtde.getTransferable())) {
            dtde.acceptDrag(DnDConstants.ACTION_LINK);
            awtCanvas.setCursor(DragSource.DefaultLinkDrop);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (isFileList(dtde.getTransferable())) {
            dtde.acceptDrag(DnDConstants.ACTION_LINK);
            awtCanvas.setCursor(DragSource.DefaultLinkDrop);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        awtCanvas.setCursor(Cursor.getDefaultCursor());
    }

    private boolean dropFile(File file, float x, float y) {
        final FileObject projectDir = mapFX.getProjectDir();
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null && FileUtil.isParentOf(projectDir, fileObject)) {
            final String relativePath = FileUtil.getRelativePath(projectDir, fileObject);
            if (relativePath != null) {
                for (FileDropListener listener : listeners) {
                    if(listener.onFileDrop(fileObject, relativePath, x, y)) {
                        return true;
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Not within project");
        }
        return false;
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        awtCanvas.setCursor(Cursor.getDefaultCursor());
        try {
            Transferable transfer = dtde.getTransferable();
            if (isFileList(transfer)) {
                dtde.acceptDrop(DnDConstants.ACTION_LINK);
                List objects = (List) transfer.getTransferData(DataFlavor.javaFileListFlavor);
                awtCanvas.postRunnable(()-> {
                    Point location = dtde.getLocation();
                    worldPos.set(location.x, location.y);
                    camera.unproject(worldPos);
                    worldPos.x = Math.round(worldPos.x);
                    worldPos.y = Math.round(worldPos.y);
                    for (Object object : objects) {
                        if (object instanceof File) {
                            dropFile((File) object, worldPos.x, worldPos.y);
                        }
                    }
                });
            }
        } catch (UnsupportedFlavorException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        } finally {
            dtde.dropComplete(true);
        }
    }

    private boolean isFileList(Transferable transfer) {
        return transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

}

package info.lusito.mapeditor.editors.map;

import info.lusito.mapeditor.editors.map.utils.TileCopyHelper;
import info.lusito.mapeditor.utils.SimpleForm;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.common.AbstractController;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.filedrop.CanvasDropTarget;
import info.lusito.mapeditor.editors.map.model.EcoMapEntityFX;
import info.lusito.mapeditor.sharedlibgdx.input.InputForwarder;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapImageFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.model.MapInterface;
import info.lusito.mapeditor.renderers.EcoEntityLayerRendererGDX;
import info.lusito.mapeditor.renderers.EcoImageLayerRendererGDX;
import info.lusito.mapeditor.renderers.EcoMapRendererGDX;
import info.lusito.mapeditor.renderers.EcoTileLayerRendererGDX;
import info.lusito.mapeditor.editors.map.renderer.GridRenderer;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.editors.map.tools.circle.DrawCircleTool;
import info.lusito.mapeditor.editors.map.tools.polyline.DrawPolylineTool;
import info.lusito.mapeditor.editors.map.tools.rect.DrawRectTool;
import info.lusito.mapeditor.editors.map.tools.select.EntitySelectionManager;
import info.lusito.mapeditor.editors.map.tools.select.ImageSelectionManager;
import info.lusito.mapeditor.editors.map.tools.tilebrush.TileBrushTool;
import info.lusito.mapeditor.editors.map.tools.tilebucket.TileBucketTool;
import info.lusito.mapeditor.editors.map.tools.tileeraser.TileEraserTool;
import info.lusito.mapeditor.editors.map.tools.reversetilepicker.ReverseTilePickerTool;
import info.lusito.mapeditor.editors.map.tools.select.SelectTool;
import info.lusito.mapeditor.editors.map.tools.select.TileSelectionManager;
import info.lusito.mapeditor.editors.map.tools.terrainbrush.TerrainBrushTool;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesGroupAdapter;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import info.lusito.mapeditor.sharedlibgdx.input.PanInputHandler;
import info.lusito.mapeditor.sharedlibgdx.input.ZoomInputHandler;
import info.lusito.mapeditor.utils.DialogUtil;
import info.lusito.mapeditor.utils.Toast;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public class MapEditorController extends AbstractController<MapDataObject> implements ApplicationListener,
        PanInputHandler.Controller, ZoomInputHandler.Controller {

    private static final float ZOOM_UPPER_LIMIT = 256.0f;
    private static final float ZOOM_LOWER_LIMIT = 1 / 256.0f;
    private static final float ZOOM_INITIAL = 1;

    private final SimpleCamera simpleCamera = new SimpleCamera();

    private int width;
    private int height;
    private GridRenderer gridRenderer;
    private final Color backgroundColor = Color.valueOf("F0F0F0");
    private EcoMapRendererGDX renderer;
    private final List<Runnable> readyCallbacks = new CopyOnWriteArrayList();
    public final InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private float camPosX, camPosY;
    private SpriteBatch batch;
    private final EcoTileLayer backupLayer = new EcoTileLayer();
    private final Vector2 dummyVectorA = new Vector2();
    private final Vector2 dummyVectorB = new Vector2();
    private final EcoMapFX mapFX = new EcoMapFX(this);
    private EcoMap map;
    private Graphics graphics;
    private Input input;
    private InputForwarder toolInput;
    private final JToolBar toolbar;
    private final BoundingRect viewBounds = new BoundingRect();
    private TileSelectionManager tileSelectionManager;
    private EntitySelectionManager entitySelectionManager;
    private ImageSelectionManager imageSelectionManager;
    private AbstractTool lastTool;
    private SelectTool selectTool;
    private DrawCircleTool drawCircleTool;
    private DrawRectTool drawRectTool;
    private DrawPolylineTool drawPolygonTool;
    private DrawPolylineTool drawPolylineTool;
    private TileEraserTool tileEraserTool;
    private ReverseTilePickerTool reverseTilePickerTool;
    private TileBucketTool tileBucketTool;
    private TileBrushTool tileBrushTool;
    private TerrainBrushTool terrainBrushTool;
    private LwjglAWTCanvas canvas;
    private boolean toolbarInitialized;
    private PropertiesAdapter propertiesEditor;
    private JButton removeShapeButton;
    private final PropertiesGroupAdapter mapGroupProperties = new PropertiesGroupAdapter();
    private final CompressionProperty compressionProperty;

    MapEditorController(JToolBar toolbar) {
        this.toolbar = toolbar;
        mapGroupProperties.name = "Map Properties";
        mapGroupProperties.description = "Map Properties";
        compressionProperty = new CompressionProperty();
        mapGroupProperties.properties.add(compressionProperty);
    }

    @Override
    protected void onLoaded() {
        dataObject.setController(this);
    }

    @Override
    public Input getInput() {
        return input;
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    @Override
    protected void load(final InputStream stream) throws IOException {
        mapFX.load(stream);
        map = mapFX.getMap();

        // Renderer
        final EcoMapRendererGDX mapRenderer = new EcoMapRendererGDX(map);
        mapRenderer.setLayerRenderer(EcoMapLayerType.TILE, new EcoTileLayerRendererGDX(map));
        mapRenderer.setLayerRenderer(EcoMapLayerType.IMAGE, new EcoImageLayerRendererGDX(map));
        mapRenderer.setLayerRenderer(EcoMapLayerType.ENTITY, new EcoEntityLayerRendererGDX(map));
        setRenderer(mapRenderer);
        camPosX = map.size.x * map.tileSize.x / 2;
        camPosY = map.size.y * map.tileSize.y / 2;
        compressionProperty.map = map;
    }

    @Override
    public void save() throws IOException {
        FileObject fo = dataObject.getPrimaryFile();
        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            map.save(out);
            markUnmodified();
        }
    }

    public MapInterface getMap() {
        return mapFX;
    }

    void onClose() {
        mapFX.close();
        if (canvas != null) {
            canvas.stop();
        }
    }

    void onShowing() {
        if (graphics != null) {
            graphics.setContinuousRendering(true);
        }
    }

    void onHidden() {
        if (graphics != null) {
            graphics.setContinuousRendering(false);
        }
    }

    void setCanvas(LwjglAWTCanvas canvas) {
        this.canvas = canvas;
        graphics = canvas.getGraphics();
        graphics.setContinuousRendering(true);
        input = canvas.getInput();
        CanvasDropTarget dropableCanvas = new CanvasDropTarget(canvas, mapFX, simpleCamera);
    }

    public void setPropertiesEditor(PropertiesAdapter properties) {
        properties.loaded = true;
        this.propertiesEditor = properties;
        updatePropertiesEditor();
    }
    
    public PropertiesAdapter getPropertiesEditor() {
        return propertiesEditor;
    }

    public void updatePropertiesEditor() {
        propertiesEditor.groups.clear();
        propertiesEditor.groups.add(mapGroupProperties);
        propertiesEditor.loaded = true;
        propertiesEditor.title = dataObject.getPrimaryFile().getNameExt();
        propertiesEditor.instance = false;
        propertiesEditor.updateEverything();
    }

    public void requestRendering() {
        graphics.requestRendering();
    }

    @Override
    public void create() {
        // This gets called on context recreation too (for example when maximizing the editor)
        if (batch != null) {
            batch.dispose();
            batch = new SpriteBatch(5460);
            return;
        }
        batch = new SpriteBatch(5460);
        gridRenderer = new GridRenderer();
        for (Runnable cb : readyCallbacks) {
            cb.run();
        }
        readyCallbacks.clear();
        input.setInputProcessor(inputMultiplexer);

        inputMultiplexer.addProcessor(new PanInputHandler(this));
        inputMultiplexer.addProcessor(new ZoomInputHandler(this));
        toolInput = new InputForwarder();
        inputMultiplexer.addProcessor(toolInput);

        simpleCamera.setZoom(ZOOM_INITIAL);
        simpleCamera.update(0);
        setupToolbar();
        mapFX.applyFocusLayer();

        final SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.addImageReloader((GameProject) getProject());
    }

    @Override
    public void moveCamera(int diffX, int diffY) {
        camPosX += diffX;
        camPosY += diffY;
        updateCameraPosition();
    }

    public void onReady(Runnable runnable) {
        readyCallbacks.add(runnable);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        simpleCamera.resize(width, height);
        updateCameraPosition();
    }

    @Override
    public void setZoom(float value) {
        if (value < ZOOM_LOWER_LIMIT) {
            value = ZOOM_LOWER_LIMIT;
        } else if (value > ZOOM_UPPER_LIMIT) {
            value = ZOOM_UPPER_LIMIT;
        }
        float zoom = getZoom();
        if (value != zoom) {
            zoom = value;

            Vector2 worldMouse = dummyVectorA.set(Gdx.input.getX(), Gdx.input.getY());
            simpleCamera.unproject(worldMouse);

            int dx = Gdx.input.getX() - Gdx.graphics.getWidth() / 2;
            int dy = Gdx.input.getY() - Gdx.graphics.getHeight() / 2;

            camPosX = Math.round(worldMouse.x / zoom - dx);
            camPosY = Math.round(worldMouse.y / zoom - dy);

            simpleCamera.setZoom(zoom);

            updateCameraPosition();
        }
    }

    @Override
    public float getZoom() {
        return simpleCamera.getZoom();
    }

    private void updateCameraPosition() {
        float x = width % 2 == 0 ? 0 : 0.5f;
        float y = height % 2 == 0 ? 0 : 0.5f;
        float zoom = simpleCamera.getZoom();
        simpleCamera.setCameraPosition(zoom * (camPosX + x), zoom * (camPosY + y));
        simpleCamera.update(0);
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setColor(Color.WHITE);
        batch.begin();

        simpleCamera.bind(batch);
        if (renderer != null) {
            Vector2 topLeft = dummyVectorA.setZero();
            simpleCamera.unproject(topLeft);
            Vector2 bottomRight = dummyVectorB.set(width, height);
            simpleCamera.unproject(bottomRight);

            renderer.update(Gdx.graphics.getDeltaTime());
            float zoom = simpleCamera.getZoom();
            viewBounds.set(
                    (int) Math.floor(topLeft.x),
                    (int) Math.floor(topLeft.y),
                    (int) Math.ceil(bottomRight.x - topLeft.x),
                    (int) Math.ceil(bottomRight.y - topLeft.y));
            renderer.renderLayers(batch, viewBounds);

            tileSelectionManager.render(batch, viewBounds);
            imageSelectionManager.render(batch, viewBounds);
            entitySelectionManager.render(batch, viewBounds);

            gridRenderer.render(batch, map.size.x, map.size.y, map.tileSize.x, map.tileSize.y, zoom);
            selectTool.render(batch, zoom);
            drawCircleTool.render(batch, zoom);
            drawRectTool.render(batch, zoom);
            drawPolygonTool.render(batch, zoom);
            drawPolylineTool.render(batch, zoom);
        }

        batch.flush();
        batch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    void setRenderer(EcoMapRendererGDX renderer) {
        this.renderer = renderer;
    }

    public EcoTileLayer getFocusTileLayer() {
        EcoMapLayerFX focusLayerFx = mapFX.getFocusLayerFX();
        if (focusLayerFx != null) {
            EcoMapLayer focusLayer = focusLayerFx.getLayer();
            if (focusLayer.getType() == EcoMapLayerType.TILE) {
                return (EcoTileLayer) focusLayer;
            }
        }
        return null;
    }

    public void setupToolbar() {
        selectTool = new SelectTool(simpleCamera, mapFX, backupLayer);
        tileSelectionManager = new TileSelectionManager(this);
        entitySelectionManager = new EntitySelectionManager(this);
        imageSelectionManager = new ImageSelectionManager(this);
        tileSelectionManager.setMap(map);
        toolbar.add(createToolButton(selectTool));
        toolbar.addSeparator();
        tileBrushTool = new TileBrushTool(simpleCamera, mapFX, backupLayer, tileSelectionManager);
        toolbar.add(createToolButton(tileBrushTool));
        terrainBrushTool = new TerrainBrushTool(simpleCamera, mapFX, backupLayer, tileSelectionManager);
        toolbar.add(createToolButton(terrainBrushTool));
        tileBucketTool = new TileBucketTool(simpleCamera, mapFX, tileSelectionManager);
        toolbar.add(createToolButton(tileBucketTool));
        toolbar.add(tileBucketTool.getGlobalModeButton());
        reverseTilePickerTool = new ReverseTilePickerTool(simpleCamera, mapFX, backupLayer);
        toolbar.add(createToolButton(reverseTilePickerTool));
        tileEraserTool = new TileEraserTool(simpleCamera, mapFX, backupLayer, tileSelectionManager);
        toolbar.add(createToolButton(tileEraserTool));
        toolbar.addSeparator();
        drawPolylineTool = new DrawPolylineTool(simpleCamera, mapFX, false);
        toolbar.add(createToolButton(drawPolylineTool));
        drawPolygonTool = new DrawPolylineTool(simpleCamera, mapFX, true);
        toolbar.add(createToolButton(drawPolygonTool));
        drawRectTool = new DrawRectTool(simpleCamera, mapFX);
        toolbar.add(createToolButton(drawRectTool));
        drawCircleTool = new DrawCircleTool(simpleCamera, mapFX);
        toolbar.add(createToolButton(drawCircleTool));
        removeShapeButton = createToolButton("removeshape", "Remove the shape from the selected entity",
                "remove shape", this::removeShape );
        toolbar.add(removeShapeButton);
        toolbar.addSeparator();
        toolbar.add(createToolButton("resize", this::showResizeMapDialog ));
        toolbar.addSeparator();
        toolbar.add(createToolButton("properties", this::showProperties ));
//        toolbar.addSeparator();
//        toolbar.add(new JLabel("Zoom: "));
//        final JTextField zoomField = new JTextField("100%");
//        zoomField.setMaximumSize(new Dimension(50, 50));
//        zoomField.setHorizontalAlignment(SwingConstants.RIGHT);
//        toolbar.add(zoomField);
        setTool(selectTool);
        toolbarInitialized = true;
    }

    protected JToggleButton createToolButton(AbstractTool tool) {
        JToggleButton button = tool.getButton();
        button.addActionListener((e) -> setTool(tool));
        return button;
    }
    
    protected JButton createToolButton(String text, ActionListener runnable) {
        JButton button = new JButton(text);
        button.addActionListener(runnable);
        return button;
    }

    protected JButton createToolButton(String imageName, String toolTipText, String altText, ActionListener runnable) {
        ImageIcon icon = new ImageIcon(AbstractTool.class.getResource(imageName + ".png"), altText);
        JButton button = new JButton(icon);
        button.setToolTipText(toolTipText);
        button.addActionListener(runnable);
        return button;
    }

    public void setSelectTool() {
        setTool(selectTool);
    }

    private void setTool(AbstractTool tool) {
        if (lastTool != tool) {
            if (lastTool != null) {
                lastTool.deselect();
            }
            if (tool != null) {
                toolInput.set(tool.getInput());
                tool.select();
            } else {
                toolInput.set(null);
            }
            lastTool = tool;
        }
    }
    
    private void removeShape(ActionEvent e) {
        EcoMapLayerFX focusLayer = mapFX.getFocusLayerFX();
        if(focusLayer.selection.size() == 1) {
            EcoMapEntity entity = (EcoMapEntity)focusLayer.selection.get(0);
            if(entity.shape != null) {
                EcoShape backupShape = entity.shape;
                entity.shape = null;
                addUndoableShapeChange(entity, entity.x, entity.y, backupShape);
            }
        } else {
            showSingleEntitySelectionRequirement();
        }
    }

    public void showSingleEntitySelectionRequirement() {
        Toast.showWarning("Select exactly one entity",
                "To change an entities shape, you need to select exactly one entity", null);
    }
    
    public void addUndoableShapeChange(EcoMapEntity entity, float oldX, float oldY, EcoShape oldShape) {
        addUndoableEdit(new UndoableShapeChange(this, entity, oldX, oldY, oldShape));
    }
    
    private void showResizeMapDialog(ActionEvent e) {
        SimpleForm.Entry left = new SimpleForm.Entry("Add/remove tiles left:", "0", 10);
        SimpleForm.Entry top = new SimpleForm.Entry("Add/remove tiles top:", "0", 10);
        SimpleForm.Entry right = new SimpleForm.Entry("Add/remove tiles right:", "0", 10);
        SimpleForm.Entry bottom = new SimpleForm.Entry("Add/remove tiles bottom:", "0", 10);
        SimpleForm.Entry[] entries = {left, top, right, bottom};
        if(DialogUtil.promptForm(entries, "Resize Map")) {
            applyResize(left.getInteger(), top.getInteger(), right.getInteger(), bottom.getInteger());
        }
    }

    private void applyResize(int left, int top, int right, int bottom) {
        if(left != 0 || right != 0 || top != 0 || bottom != 0) {
            int newWidth = map.size.x + left + right;
            int newHeight = map.size.y + top + bottom;
            if(newWidth <= 0 || newHeight <= 0) {
                DialogUtil.message("New map size is not possible: "
                        + newWidth + "x" + newHeight, DialogUtil.Icon.ERROR);
                return;
            }
            TileCopyHelper ci = new TileCopyHelper(map.size.x, map.size.y, newWidth, newHeight, left, top);
            float xOffset = left * map.tileSize.x;
            float yOffset = top * map.tileSize.y;
            UndoableMapResize change = new UndoableMapResize(this, map.size.x, map.size.y, newWidth, newHeight,
                    xOffset, yOffset);
            for (EcoMapLayer layer : map.layers) {
                if(layer.getType() == EcoMapLayerType.TILE) {
                        EcoTileLayer tileLayer = (EcoTileLayer)layer;
                        EcoTileInfo[][] tiles = new EcoTileInfo[newWidth][newHeight];
                        ci.copyTiles(tileLayer.tiles, tiles);
                        change.pushTileLayerChange(tileLayer, tileLayer.tiles, tiles);
                        tileLayer.tiles = tiles;
                }
            }
            if(xOffset != 0 || yOffset != 0)
                offsetEntitiesAndImages(xOffset, yOffset);
            camPosX += xOffset;
            camPosY += yOffset;
            updateCameraPosition();
            map.size.x = newWidth;
            map.size.y = newHeight;
            
            addUndoableEdit(change);
        }
    }

    private void offsetEntitiesAndImages(float x, float y) {
        for (EcoMapLayer layer : map.layers) {
            switch (layer.getType()) {
                case ENTITY:
                    EcoEntityLayer entityLayer = (EcoEntityLayer)layer;
                    for (EcoMapEntity entity : entityLayer.entities) {
                        entity.x += x;
                        entity.y += y;
                        EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
                        entityFX.updateRect(entity);
                    }
                    break;
                case IMAGE:
                    EcoImageLayer imageLayer = (EcoImageLayer)layer;
                    for (EcoMapImage image : imageLayer.images) {
                        image.x += x;
                        image.y += y;
                        EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
                        imageFX.updateRect(image);
                    }
                    break;
            }
        }
    }
    

    private void showProperties(ActionEvent e) {
        updatePropertiesEditor();
    }

    public void setFocusLayer(EcoMapLayerFX layer, FocusMode mode) {
        if (!toolbarInitialized) {
            return;
        }
        boolean refocusSelection = false;
        switch(mode) {
            case MAP:
                updatePropertiesEditor();
                break;
            case LAYER:
                layer.showProperties();
                break;
            case SELECTION:
                refocusSelection = true;
                break;
        }
        boolean isTileLayer = false;
        boolean isEntityLayer = false;
        if (layer == null) {
            selectTool.setSelectionListener(null);
            tileSelectionManager.setFocusLayer(null);
            entitySelectionManager.setFocusLayer(null, null, refocusSelection);
            imageSelectionManager.setFocusLayer(null, null, refocusSelection);
            drawCircleTool.setFocusLayer(null);
            drawRectTool.setFocusLayer(null);
            drawPolygonTool.setFocusLayer(null);
            drawPolylineTool.setFocusLayer(null);
        } else {
            isTileLayer = layer.getLayer().getType() == EcoMapLayerType.TILE;
            isEntityLayer = layer.getLayer().getType() == EcoMapLayerType.ENTITY;
            switch (layer.getLayer().getType()) {
                case TILE:
                    selectTool.setSelectionListener(tileSelectionManager);
                    tileSelectionManager.setFocusLayer((EcoTileLayer) layer.getLayer());
                    entitySelectionManager.setFocusLayer(null, null, refocusSelection);
                    imageSelectionManager.setFocusLayer(null, null, refocusSelection);
                    drawCircleTool.setFocusLayer(null);
                    drawRectTool.setFocusLayer(null);
                    drawPolygonTool.setFocusLayer(null);
                    drawPolylineTool.setFocusLayer(null);
                    break;
                case ENTITY:
                    selectTool.setSelectionListener(entitySelectionManager);
                    tileSelectionManager.setFocusLayer(null);
                    final EcoEntityLayer entityLayer = (EcoEntityLayer) layer.getLayer();
                    entitySelectionManager.setFocusLayer(layer, entityLayer, refocusSelection);
                    imageSelectionManager.setFocusLayer(null, null, refocusSelection);
                    drawCircleTool.setFocusLayer(layer);
                    drawRectTool.setFocusLayer(layer);
                    drawPolygonTool.setFocusLayer(layer);
                    drawPolylineTool.setFocusLayer(layer);
                    break;
                case IMAGE:
                    selectTool.setSelectionListener(imageSelectionManager);
                    tileSelectionManager.setFocusLayer(null);
                    entitySelectionManager.setFocusLayer(null, null, refocusSelection);
                    imageSelectionManager.setFocusLayer(layer, (EcoImageLayer) layer.getLayer(), refocusSelection);
                    drawCircleTool.setFocusLayer(null);
                    drawRectTool.setFocusLayer(null);
                    drawPolygonTool.setFocusLayer(null);
                    drawPolylineTool.setFocusLayer(null);
                    break;
            }
        }
        tileEraserTool.setButtonEnabled(isTileLayer);
        reverseTilePickerTool.setButtonEnabled(isTileLayer);
        tileBucketTool.setButtonEnabled(isTileLayer);
        tileBrushTool.setButtonEnabled(isTileLayer);
        terrainBrushTool.setButtonEnabled(isTileLayer);

        drawCircleTool.setButtonEnabled(isEntityLayer);
        drawRectTool.setButtonEnabled(isEntityLayer);
        drawPolygonTool.setButtonEnabled(isEntityLayer);
        drawPolylineTool.setButtonEnabled(isEntityLayer);
        removeShapeButton.setEnabled(isEntityLayer);

        // if no enabled tool is selected, select default tool
        if (lastTool.isSelected() && !lastTool.isButtonEnabled()) {
            setTool(selectTool);
        }
    }

    public void refocusLayer(EcoMapLayerFX layerFX, FocusMode mode) {
        mapFX.refocusLayer(layerFX, mode);
    }

    public void onTileSelected() {
        //fixme: select terrainBrush if terrain tile selected?
        if(tileBrushTool != null && tileBrushTool.isButtonEnabled() &&
                !tileBrushTool.isSelected() && !tileBucketTool.isSelected()
                 && !terrainBrushTool.isSelected()) {
            setTool(tileBrushTool);
        }
    }

    private class UndoableMapResize extends UndoableMapEdit {

        private final int fromWidth;
        private final int fromHeight;
        private final int toWidth;
        private final int toHeight;
        private final float xOffset;
        private final float yOffset;
        private final List<TileLayerResize> tileLayerChanges = new ArrayList();

        public UndoableMapResize(MapEditorController controller, int fromWidth, int fromHeight, int toWidth, int toHeight, float xOffset, float yOffset) {
            super(controller);
            this.fromWidth = fromWidth;
            this.fromHeight = fromHeight;
            this.toWidth = toWidth;
            this.toHeight = toHeight;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
        
        public void pushTileLayerChange(EcoTileLayer layer, EcoTileInfo[][] from, EcoTileInfo[][] to) {
            tileLayerChanges.add(new TileLayerResize(layer, from, to));
        }

        
        public void performUndoRedo(boolean undo) {
            for (TileLayerResize change : tileLayerChanges) {
                change.layer.tiles = undo ? change.from : change.to;
            }
            float x = undo ? -xOffset : xOffset;
            float y = undo ? -yOffset : yOffset;
            if(xOffset != 0 || yOffset != 0) {
                offsetEntitiesAndImages(x, y);
            }
            camPosX += x;
            camPosY += y;
            updateCameraPosition();
            map.size.x = undo ? fromWidth : toWidth;
            map.size.y = undo ? fromHeight : toHeight;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            performUndoRedo(true);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            performUndoRedo(false);
        }

        private class TileLayerResize {
            private final EcoTileLayer layer;
            private final EcoTileInfo[][] from;
            private final EcoTileInfo[][] to;

            public TileLayerResize(EcoTileLayer layer, EcoTileInfo[][] from, EcoTileInfo[][] to) {
                this.layer = layer;
                this.from = from;
                this.to = to;
            }
        }
    }

    private class UndoableShapeChange extends UndoableMapEdit {

        private final EcoMapEntity entity;
        private final float fromX;
        private final float fromY;
        private final float toX;
        private final float toY;
        private final EcoShape fromShape;
        private final EcoShape toShape;

        public UndoableShapeChange(MapEditorController controller, EcoMapEntity entity, float oldX, float oldY, EcoShape oldShape) {
            super(controller);
            this.entity = entity;
            this.fromX = oldX;
            this.fromY = oldY;
            this.toX = entity.x;
            this.toY = entity.y;
            fromShape = oldShape;
            toShape = entity.shape;
        }
        
        public void performUndoRedo(float x, float y, EcoShape shape) {
            entity.x = x;
            entity.y = y;
            entity.shape = shape;
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            entityFX.updateRect(entity);
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            performUndoRedo(fromX, fromY, fromShape);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            performUndoRedo(toX, toY, toShape);
        }
    }
}

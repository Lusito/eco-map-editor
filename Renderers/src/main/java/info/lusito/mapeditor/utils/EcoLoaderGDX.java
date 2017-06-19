package info.lusito.mapeditor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.model.EcoMapEntityGDX;
import info.lusito.mapeditor.model.EcoMapImageGDX;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoMapTilesetReference;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import info.lusito.mapeditor.renderers.EcoEntityLayerRendererGDX;
import info.lusito.mapeditor.renderers.EcoImageLayerRendererGDX;
import info.lusito.mapeditor.renderers.EcoMapRendererGDX;
import info.lusito.mapeditor.renderers.EcoTileLayerRendererGDX;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EcoLoaderGDX {

    private final String prefix;
    private final Map<String, Texture> textures = new HashMap();
    private final Map<String, EcoEntity> entityDefinitions = new HashMap();
    private final Map<String, EcoTileset> tilesets = new HashMap();
    private final Map<String, EcoAnimation> animations = new HashMap();
    private final Preloader texturesPreloader = new Preloader(this::preloadTexture);
    private final Preloader entitiesPreloader = new Preloader(this::preloadEntity);
    private final Preloader tilesetsPreloader = new Preloader(this::preloadTileset);
    private final Preloader animationsPreloader = new Preloader(this::preloadAnimation);

    public EcoLoaderGDX(String prefix) {
        this.prefix = prefix;
        try (InputStreamReader fr = new InputStreamReader(openStream("filelist.txt"));
                BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String ext = getFileExtension(line);
                if (ext != null) {
                    switch (ext) {
                        case "png":
                        case "jpg":
                        case "jpeg":
                        case "tga":
                            texturesPreloader.files.add(line);
                            break;
                        case "xed":
                            entitiesPreloader.files.add(line);
                            break;
                        case "xad":
                            animationsPreloader.files.add(line);
                            break;
                        case "xtd":
                            tilesetsPreloader.files.add(line);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading filelist.txt: " + e.getMessage());
        }
    }

    private static String getFileExtension(String path) {
        int index = path.lastIndexOf(".");
        if (index >= 0) {
            return path.substring(index + 1).toLowerCase();
        }
        return null;
    }

    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
        entityDefinitions.clear();
        tilesets.clear();
        animations.clear();
    }

    private void preloadTexture(String filename) {
        getTexture(filename);
    }

    private void preloadEntity(String filename) {
        loadEntity(filename);
    }

    private void preloadTileset(String filename) {
        loadTileset(filename);
    }

    private void preloadAnimation(String filename) {
        loadAnimation(filename);
    }

    public int getPreloadCount() {
        return texturesPreloader.files.size()
                + animationsPreloader.files.size()
                + entitiesPreloader.files.size()
                + tilesetsPreloader.files.size();
    }

    public int getPreloadPosition() {
        return texturesPreloader.preloadPosition
                + animationsPreloader.preloadPosition
                + entitiesPreloader.preloadPosition
                + tilesetsPreloader.preloadPosition;
    }

    public float getPreloadProgress() {
        if(getPreloadCount() == 0)
            return 1;
        return (float)getPreloadPosition() / (float)getPreloadCount();
    }

    public void preloadNext() {
        if (texturesPreloader.preloadNext()
                || animationsPreloader.preloadNext()
                || entitiesPreloader.preloadNext()
                || tilesetsPreloader.preloadNext()) {
        }
    }

    public Texture getTexture(String filename) {
        Texture texture = textures.get(filename);
        if (texture == null) {
            texture = new Texture(prefix + filename);
            textures.put(filename, texture);
        }
        return texture;
    }

    private InputStream openStream(String filename) {
        return Gdx.files.internal(prefix + filename).read();
    }

    public EcoMap loadMap(String filename, boolean loadEntityTextures) {
        try (InputStream mapStream = openStream(filename)) {
            EcoMap map = EcoMap.load(mapStream);

            for (EcoMapTilesetReference ref : map.tilesets) {
                ref.tileset = loadTileset(ref.src);
            }
            if (!map.layers.isEmpty()) {
                for (EcoMapLayer layer : map.layers) {
                    layer.onAfterRead(map);
                    switch (layer.getType()) {
                        case ENTITY:
                            EcoEntityLayer entityLayer = (EcoEntityLayer) layer;
                            for (EcoMapEntity entity : entityLayer.entities) {
                                EcoEntity ed = loadEntity(entity.type);
                                Texture texture = loadEntityTextures ? getTexture(ed.image) : null;
                                entity.attachment = new EcoMapEntityGDX(entity, ed, texture);
                            }
                            break;
                        case IMAGE:
                            EcoImageLayer imageLayer = (EcoImageLayer) layer;
                            for (EcoMapImage image : imageLayer.images) {
                                image.attachment = loadMapImage(image);
                            }
                            break;
                    }
                }
            }
            return map;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Map konnte nicht geladen werden: " + filename, e);
        }
    }

    public EcoAnimation loadAnimation(String filename) {
        try (InputStream stream = openStream(filename)) {
            EcoAnimation animation = EcoAnimation.load(stream);
            animations.put(filename, animation);
            return animation;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Animation konnte nicht geladen werden: " + filename, e);
        }
    }

    private EcoMapImageGDX loadMapImage(EcoMapImage image) {
        boolean isAnimation = image.filename.toLowerCase().endsWith(".xad");
        if (isAnimation) {
            EcoAnimation animation = loadAnimation(image.filename);
            Texture texture = getTexture(animation.image.src);
            return new EcoMapImageGDX(image, texture, animation);
        } else {
            return new EcoMapImageGDX(image, getTexture(image.filename));
        }
    }

    public EcoEntity loadEntity(String filename) {
        EcoEntity ed = entityDefinitions.get(filename);
        if (ed == null) {
            try (InputStream stream = openStream(filename)) {
                ed = EcoEntity.load(stream);
                entityDefinitions.put(filename, ed);
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Entity konnte nicht geladen werden: " + filename, e);
            }
        }
        return ed;
    }

    private EcoTileset loadTileset(String filename) {
        EcoTileset tileset = tilesets.get(filename);
        if (tileset == null) {
            try (InputStream stream = openStream(filename)) {
                tileset = EcoTileset.load(stream);
                tileset.attachment = getTexture(tileset.image.src);
                tilesets.put(filename, tileset);
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Tileset konnte nicht geladen werden: " + filename, e);
            }
        }
        return tileset;
    }

    public EcoMapRendererGDX createRenderer(EcoMap map, boolean entityRenderer) {
        if (map != null) {
            EcoMapRendererGDX mapRenderer = new EcoMapRendererGDX(map);
            mapRenderer.setLayerRenderer(EcoMapLayerType.TILE, new EcoTileLayerRendererGDX(map));
            mapRenderer.setLayerRenderer(EcoMapLayerType.IMAGE, new EcoImageLayerRendererGDX(map));
            if (entityRenderer) {
                mapRenderer.setLayerRenderer(EcoMapLayerType.ENTITY, new EcoEntityLayerRendererGDX(map));
            }
            return mapRenderer;
        }
        return null;
    }

    public Sound loadSound(String string) {
        return null; //fixme
    }

    private class Preloader {

        final Consumer<String> consumer;
        final List<String> files = new ArrayList();
        int preloadPosition;

        public Preloader(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        public boolean preloadNext() {
            if (preloadPosition < files.size()) {
                final String file = files.get(preloadPosition);
                if(Gdx.files.internal(file).exists()) {
                    consumer.accept(file);
                }
                preloadPosition++;
                return true;
            }
            return false;
        }
    }
}

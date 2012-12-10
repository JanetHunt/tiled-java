/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.core;

import java.util.*;

import tiled.mapeditor.Resources;

/**
 * The Map class is the focal point of the <code>tiled.core</code> package.
 * This class also handles notifying listeners if there is a change to any layer
 * or object contained by the map.
 *
 * @version $Id$
 */
public class Map extends MultilayerPlane implements MapLayerChangeListener
{
    /** Orthogonal. */
    public static final int MDO_ORTHO   = 1;
    /** Isometric. */
    public static final int MDO_ISO     = 2;
    /** Hexagonal. */
    public static final int MDO_HEX     = 4;
    /** Shifted (used for iso and hex). */
    public static final int MDO_SHIFTED = 5;

    private Vector<MapLayer> specialLayers;
    private ArrayList<TileSet> tilesets;
    private LinkedList<MapObject> objects;
    private final ArrayList<MapChangeListener> mapChangeListeners = new ArrayList<MapChangeListener>();
    private final ArrayList<MapParallaxChangeListener> mapParallaxChangeListeners = new ArrayList<MapParallaxChangeListener>();
    private Properties properties = new Properties();

    private int orientation = MDO_ORTHO;
    private String filename;
    private int tileWidth;
    private int tileHeight;
    private float eyeDistance = 100;
    private int viewportWidth = 640;
    private int viewportHeight = 480;
        
    /**
     * @param width  the map width in tiles.
     * @param height the map height in tiles.
     */
    public Map(int width, int height) {
        super(width, height);

        tilesets = new ArrayList<TileSet>();
        specialLayers = new Vector<MapLayer>();
        objects = new LinkedList<MapObject>();
    }

    /**
     * Adds a change listener. The listener will be notified when the map
     * changes in certain ways.
     *
     * @param listener the change listener to add
     * @see MapChangeListener#mapChanged(MapChangedEvent)
     */
    public void addMapChangeListener(MapChangeListener listener) {
        synchronized ( mapChangeListeners ) {
            if ( !mapChangeListeners.contains(listener) ) {
                mapChangeListeners.add(listener);
            }
        }
    }

    /**
     * Removes a change listener.
     * @param listener the listener to remove
     */
    public void removeMapChangeListener(MapChangeListener listener) {
        synchronized ( mapChangeListeners ) {
             mapChangeListeners.remove(listener);
        }
    }

    public void addMapParallaxChangeListener(MapParallaxChangeListener listener){
        synchronized ( mapParallaxChangeListeners ) {
            if ( !mapParallaxChangeListeners.contains(listener) ) {
                mapParallaxChangeListeners.add(listener);
            }
        }
    }
    
    public void removeMapParallaxChangeListener(MapParallaxChangeListener listener){
        synchronized ( mapParallaxChangeListeners ) {
            mapParallaxChangeListeners.remove(listener);
        }
    }
    
    protected Iterable<MapChangeListener> getMapChangeListenerIterable () {
        synchronized (mapChangeListeners) {
            // clone mapChangeListeners first, because otherwise we'll get
            // concurrent modification exceptions if a listener calls something
            // that add or removes listeners
            @SuppressWarnings("unchecked")
            Iterable<MapChangeListener> mapChangeListenersClone = 
                    (ArrayList<MapChangeListener>) mapChangeListeners.clone();
            return mapChangeListenersClone;
        }
    }
        
    /**
     * Notifies all registered map change listeners about a change.
     */
    protected void fireMapChanged() {
        MapChangedEvent event = new MapChangedEvent(this);
        for(MapChangeListener l : getMapChangeListenerIterable()) {
            l.mapChanged(event);
        }
    }
    
    protected void fireLayerRemoved(int layerIndex){
        MapChangedEvent e = new MapChangedEvent(this, layerIndex); 
        for(MapChangeListener l : getMapChangeListenerIterable()){
            l.layerRemoved(e);
        }
    }

    protected void fireLayerAdded(int layerIndex){
        MapChangedEvent e = new MapChangedEvent(this, layerIndex); 
        for(MapChangeListener l : getMapChangeListenerIterable()){
            l.layerAdded(e);
        }
    }
    
    protected void fireLayerMoved(int oldLayerIndex, int newLayerIndex){
        MapChangedEvent e = new MapChangedEvent(this, newLayerIndex, oldLayerIndex);
        for(MapChangeListener l : getMapChangeListenerIterable()){
            l.layerMoved(e);
        }
    }
    
    protected void fireLayerChanged(int layerIndex, MapLayerChangeEvent mlce){
        MapChangedEvent e = new MapChangedEvent(this, layerIndex);
        for(MapChangeListener l : getMapChangeListenerIterable())
            l.layerChanged(e, mlce);
    }
    /**
     * Notifies all registered map change listeners about the removal of a
     * tileset.
     *
     * @param index the index of the removed tileset
     */
    protected void fireTilesetRemoved(int index) {
        MapChangedEvent e = new MapChangedEvent(this);
        for(MapChangeListener l : getMapChangeListenerIterable())
            l.tilesetRemoved(e, index);
    }

    /**
     * Notifies all registered map change listeners about the addition of a
     * tileset.
     *
     * @param tileset the new tileset
     */
    protected void fireTilesetAdded(TileSet tileset) {
        MapChangedEvent e = new MapChangedEvent(this);
        for(MapChangeListener l : getMapChangeListenerIterable())
            l.tilesetAdded(e, tileset);
    }

    /**
     * Notifies all registered map change listeners about the reorder of the
     * tilesets.
     */
    protected void fireTilesetsSwapped(int index0, int index1) {
        MapChangedEvent e = new MapChangedEvent(this);
        for(MapChangeListener l : getMapChangeListenerIterable())
            l.tilesetsSwapped(e, index0, index1);
    }

    @SuppressWarnings("unchecked")
    void fireParallaxChangeEvent(MapParallaxChangeEvent mapParallaxChangeEvent) {
        Iterable<MapParallaxChangeListener> listClone; 
        synchronized (mapParallaxChangeListeners) {
            listClone = (ArrayList<MapParallaxChangeListener>) mapParallaxChangeListeners.clone();
        }
        for(MapParallaxChangeListener l : listClone){
            l.parallaxParameterChanged(mapParallaxChangeEvent);
        }
    }

    public void layerChanged(MapLayer layer, MapLayerChangeEvent e) {
        fireLayerChanged(findLayerIndex(layer), e);
    }
    
    /**
     * Causes a MapChangedEvent to be fired.
     */
    public void touch() {
        fireMapChanged();
    }

    public void addLayerSpecial(MapLayer layer) {
        layer.setMap(this);
        specialLayers.add(layer);
        fireMapChanged();
    }

    @Override
    public MapLayer addLayer(MapLayer layer) {
        layer.setMap(this);
        super.addLayer(layer);
        layer.addMapLayerChangeListener(this);
        fireMapChanged();
        fireLayerAdded(getLayerVector().indexOf(layer));
        return layer;
    }

    /**
     * Create a new empty TileLayer with the dimensions of the map. By default,
     * the new layer's name is set to "Layer [layer index]"
     *
     * @return The new TileLayer instance.
     */
    public MapLayer addLayer() {
        MapLayer layer = new TileLayer(this, bounds.width, bounds.height);
        layer.setName(Resources.getString("general.layer.layer") +
                      " " + super.getTotalLayers());
        insertLayer(getTotalLayers(), layer);
        return layer;
    }

    public void insertLayer(int index, MapLayer layer) {
        super.insertLayer(index, layer);
        layer.addMapLayerChangeListener(this);
        fireMapChanged();
        fireLayerAdded(index);
    }

    public void setLayer(int index, MapLayer layer) {
        boolean isNew = index == getTotalLayers(); 
        layer.setMap(this);
        super.setLayer(index, layer);
        fireMapChanged();
        if ( !isNew ) {
            fireLayerRemoved(index);
        }
        fireLayerAdded(index);
    }

    /**
     * Create a new empty ObjectGroup. By default, the new layer's name is set
     * to "ObjectGroup [layer index]"
     *
     * @return The new ObjectGroup instance.
     */
    public MapLayer addObjectGroup() {
        MapLayer layer = new ObjectGroup(this);
        layer.setName(Resources.getString("general.objectgroup.objectgroup") +
                      " " + super.getTotalLayers());
        fireMapChanged();
        super.addLayer(layer);
        return layer;
    }

    /**
     * Adds a Tileset to this Map. Does nothing if <code>tileset</code> is <b>null</b> or already 
     * attached to this map.
     *
     * @param tileset a tileset to add
     */
    public void addTileset(TileSet tileset) {
        if (tileset == null || tilesets.indexOf(tileset) > -1) {
            return;
        }

        Tile t = tileset.getTile(0);

        if (t != null) {
            int tw = t.getWidth();
            int th = t.getHeight();
            if (tw != tileWidth) {
                if (tileWidth == 0) {
                    tileWidth = tw;
                    tileHeight = th;
                }
            }
        }

        tilesets.add(tileset);
        fireTilesetAdded(tileset);
    }

    /**
     * Removes a {@link TileSet} from the map, and removes any tiles in the set
     * from the map layers. A {@link MapChangedEvent} is fired when all
     * processing is complete.
     *
     * @param tileset TileSet to remove
     * @throws LayerLockedException when the tileset is in use on a locked
     *         layer
     */
    public void removeTileset(TileSet tileset) throws LayerLockedException {
        // Sanity check
        final int tilesetIndex = tilesets.indexOf(tileset);
        if (tilesetIndex == -1)
            return;

        // Go through the map and remove any instances of the tiles in the set
        Iterator<Tile> tileIterator = tileset.iterator();
        while (tileIterator.hasNext()) {
            Tile tile = tileIterator.next();
            Iterator<MapLayer> layerIterator = getLayers();
            while (layerIterator.hasNext()) {
                MapLayer ml = (MapLayer) layerIterator.next();
                if (ml instanceof TileLayer) {
                    ((TileLayer) ml).removeTile(tile);
                }
            }
        }

        tilesets.remove(tileset);
        fireTilesetRemoved(tilesetIndex);
    }

    public void addObject(MapObject o) {
        objects.add(o);
    }

    public Iterator<MapObject> getObjects() {
        return objects.iterator();
    }

    /**
     * @return the map properties
     */
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties prop) {
        properties = prop;
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#removeLayer(int)
     */
    public MapLayer removeLayer(int index) {
        MapLayer layer = super.removeLayer(index);
        layer.removeMapLayerChangeListener(this);
        fireMapChanged();
        fireLayerRemoved(index);
        return layer;
    }

    public void removeLayerSpecial(MapLayer layer) {
        if (specialLayers.remove(layer)) {
            fireMapChanged();
        }
    }

    public void removeAllSpecialLayers() {
        specialLayers.clear();
        fireMapChanged();
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#removeAllLayers
     */
    public void removeAllLayers() {
        while(getTotalLayers() > 0){
            getLayer(0).removeMapLayerChangeListener(this);
            removeLayer(0);
            fireLayerRemoved(0);
        }
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#setLayerVector
     */
    public void setLayerVector(Vector<MapLayer> layers) {
        super.setLayerVector(layers);
        fireMapChanged();
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#swapLayerUp
     */
    public void swapLayerUp(int index) {
        super.swapLayerUp(index);
        fireMapChanged();
        fireLayerMoved(index, index+1);
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#swapLayerDown
     */
    public void swapLayerDown(int index) {
        super.swapLayerDown(index);
        fireMapChanged();
        fireLayerMoved(index, index-1);
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#mergeLayerDown
     */
    public void mergeLayerDown(int index) {
        super.mergeLayerDown(index);
        fireMapChanged();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Sets a new tile width.
     *
     * @param width the new tile width
     */
    public void setTileWidth(int width) {
        tileWidth = width;
        fireMapChanged();
    }

    /**
     * Sets a new tile height.
     *
     * @param height the new tile height
     */
    public void setTileHeight(int height) {
        tileHeight = height;
        fireMapChanged();
    }

    /**
     * Calls super method, and additionally fires a {@link MapChangedEvent}.
     *
     * @see MultilayerPlane#resize
     */
    public void resize(int width, int height, int dx, int dy) {
        super.resize(width, height, dx, dy);
        fireMapChanged();
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        // TODO: fire mapChangedNotification about orientation change
    }

    public String getFilename() {
        return filename;
    }

    public Iterator<MapLayer> getLayersSpecial() {
        return specialLayers.iterator();
    }

    /**
     * Returns a list with the currently loaded tilesets.
     *
     * @return <code>List</code>
     */
    @SuppressWarnings("unchecked")
    public List<TileSet> getTilesets() {
        return (ArrayList<TileSet>)tilesets.clone();
    }

    /**
     * Get the tile set that matches the given global tile id, only to be used
     * when loading a map.
     *
     * @param gid a global tile id
     * @return the tileset containing the tile with the given global tile id,
     *         or <code>null</code> when no such tileset exists
     */
    public TileSet findTileSetForTileGID(int gid) {
        TileSet has = null;
        for (TileSet tileset : tilesets) {
            if (tileset.getFirstGid() <= gid) {
                has = tileset;
            }
        }
        return has;
    }

    /**
     * Returns width of map in tiles.
     *
     * @return int
     */
    public int getWidth() {
        return bounds.width;
    }

    /**
     * Returns height of map in tiles.
     *
     * @return int
     */
    public int getHeight() {
        return bounds.height;
    }

    /**
     * Returns default tile width for this map.
     *
     * @return the default tile width
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Returns default tile height for this map.
     *
     * @return the default tile height
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Returns wether the given tile coordinates fall within the map
     * boundaries.
     *
     * @param x The tile-space x-coordinate
     * @param y The tile-space y-coordinate
     * @return <code>true</code> if the point is within the map boundaries,
     *         <code>false</code> otherwise
     */
    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < bounds.width && y < bounds.height;
    }

    /**
     * Returns the maximum tile height. This is the height of the highest tile
     * in all tilesets or the tile height used by this map if it's smaller.
     *
     * @return int The maximum tile height
     */
    public int getTileHeightMax() {
        int maxHeight = tileHeight;

        for (TileSet tileset : tilesets) {
            int height = tileset.getTileHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        return maxHeight;
    }

    /**
     * Swaps the tile sets at the given indices.
     */
    public void swapTileSets(int index0, int index1) {
        if (index0 == index1) return;
        TileSet set = tilesets.get(index0);
        tilesets.set(index0, tilesets.get(index1));
        tilesets.set(index1, set);

        if (index0 > index1) {
            int temp = index1;
            index1 = index0;
            index0 = temp;
        }

        fireTilesetsSwapped(index0, index1);
    }

    /**
     * Returns the sum of the size of each tile set.
     *
     * @return
     */
    /*
    public int getTotalTiles() {
        int totalTiles = 0;
        Iterator itr = tilesets.iterator();

        while (itr.hasNext()) {
            TileSet cur = (TileSet)itr.next();
            totalTiles += cur.getTotalTiles();
        }

        return totalTiles;
    }
    */

    /**
     * Returns the orientation of this map. Orientation will be one of
     * {@link Map#MDO_ISO}, {@link Map#MDO_ORTHO}, {@link Map#MDO_HEX},
     * and {@link Map#MDO_SHIFTED}.
     *
     * @return The orientation from the enumerated set
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Returns string describing the map. The form is <code>Map[width x height
     * x layers][tileWidth x tileHeight]</code>, for example <code>
     * Map[64x64x2][24x24]</code>.
     *
     * @return string describing map
     */
    public String toString() {
        return "Map[" + bounds.width + "x" + bounds.height + "x" +
            getTotalLayers() + "][" + tileWidth + "x" +
            tileHeight + "]";
    }

    public float getEyeDistance() {
        return eyeDistance;
    }

    public void setEyeDistance(float eyeDistance) {
        if(this.eyeDistance == eyeDistance)
            return;
        this.eyeDistance = eyeDistance;
        fireParallaxChangeEvent(new MapParallaxChangeEvent(this, -1, MapParallaxChangeEvent.ChangeType.EYE_VIEWPLANE_DISTANCE));
    }

    public int getViewportWidth() {
        return viewportWidth;
    }
    
    public int getViewportHeight() {
        return viewportHeight;
    }
    
    public void setViewportWidth(int w) {
        viewportWidth = w;
    }
    
    public void setViewportHeight(int h) {
        viewportHeight = h;
    }

}

package lanse.worldwarchess.shared.core.piece.marker;

import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Marker {

    //TODO - add size parameter to marker and fix drawing inconsistencies with markers.

    //This is the super class for all marker types, which eventually will be like death markers, magic attack markers,
    //or other indicators from pieces.
    public static List<Marker> markers = new CopyOnWriteArrayList<>();

    protected final Board board;
    protected final int posX;
    protected final int posY;
    protected final Color color;
    protected int lifeSpan;
    protected final int size;

    public Marker(Board board, int posX, int posY, Color color, int lifeSpan, int size){
        this.board = board;
        this.posX = posX;
        this.posY = posY;
        this.color = color;
        this.lifeSpan = lifeSpan;
        this.size = size;

        markers.add(this);
    }

    public static void tickMarkers() { for (Marker marker : markers) marker.tick(); }

    public Board getBoard() { return board; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public Color getColor() { return color; }
    public int getLifeSpan() { return lifeSpan; }

    public void tick(){
        if (lifeSpan == -40404) return;

        lifeSpan--;

        if (lifeSpan <= 0){
            Marker.markers.remove(this);
            this.onLifeSpanEnding();
        }
    }

    public void onLifeSpanEnding(){}


    public static void drawAllMarkers(double zoomFactor, Graphics2D g2d) {
        boolean showDeathMarkers = Settings.isShowDeathMarkers();

        for (Marker marker : markers) {
            if (!showDeathMarkers && marker instanceof DeathMarker) continue;

            Board board = marker.getBoard();
            if (board == null) continue;

            int baseTile = board.getBaseTileSize();
            double scaledTile = baseTile * zoomFactor;

            double drawX = board.getBoardX() * zoomFactor + marker.getPosX() * scaledTile;
            double drawY = board.getBoardY() * zoomFactor + marker.getPosY() * scaledTile;

            int markerSize = (int) Math.round(scaledTile * marker.size);

            int px = (int) Math.round(drawX);
            int py = (int) Math.round(drawY);

            g2d.setColor(marker.getColor());
            g2d.fillRect(px, py, markerSize, markerSize);
        }
    }
}

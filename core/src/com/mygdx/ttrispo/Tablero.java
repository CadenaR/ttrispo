package com.mygdx.ttrispo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.ttrispo.Pantalla.PiezaT;

public class Tablero extends Actor {
    public static int tablero[][];
    private Texture img, img2;
    //private PiezaT a;
    private int as = 0, asd[];

    public static final int size = 25; //pieza
    public static int TableroX = size * 10;
    public static int TableroY = size * 20;



    public Tablero() {
        this.tablero = new int[TableroX/size][TableroY/size];
        img = new Texture("L.jpg");
        img2 = new Texture("bg_tablero.png");
        this.setWidth(img.getWidth());
        this.setHeight(img.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        int x,y;
        for (int i = 0; i < this.tablero.length; i++) {
            for (int j = 0; j < this.tablero[i].length; j++) {
                if(this.tablero[i][j] >= 1){
                    x =  80*i;
                    y =  Gdx.graphics.getHeight() - 80*j;
                    batch.draw(img,x,y,0,0,75,75);
                }if(this.tablero[i][j] == 0){
                    x =  80*i;
                    y =  Gdx.graphics.getHeight() - 80*j;
                    batch.draw(img2,x,y,0,0,70,70);
                }
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

    }

    public void cambiarBloque(int bloques[][], int tipo) {
        int columnas, filas;
        for (int i = 0; i < bloques.length; i++) {
            columnas = bloques[i][1];
            filas = bloques[i][0];
            // Comprobar si se sale de la pantalla
            if(columnas < 10 && columnas >= 0 && filas >= 0 && filas < 20){
                tablero[bloques[i][1]][bloques[i][0]] = tipo;
            }
        }
    }

   /*
   Este metodo es dios en la tierra.
   Implemento un try catch para cuando se sale de array.
    */

    public boolean isColision(int bloques[][]){
        int columnas, filas;
        try {
            for (int i = 0; i < bloques.length; i++) {
                columnas = bloques[i][1];
                filas = bloques[i][0];
                // Comprobar si se sale de la pantalla
                if (columnas >= 10 || filas >= 20) {
                    return true;
                }
                // Colisiona con otro bloque
                if (tablero[bloques[i][1]][bloques[i][0]] != 0) {
                    return true;
                }
            }
        }catch (ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }

    public boolean comprobarLineaCompleta() {
        int numeroColumnas = 10;
        int filas = 20;
        int valorFila = 0;
        for(int i=0;i<this.tablero.length;i++){
            for(int j=0; j<this.tablero[i].length;j++){
                valorFila += tablero[i][j];
            }

            if(valorFila==numeroColumnas){
                System.out.println("Premio");
                eliminarfila(i);
                //bajarFilaAnterior();
            }

            valorFila =0;
        }
        return false;
    }

    private void eliminarfila(int fila) {
        for(int j=0; j<this.tablero[fila].length;j++){
            tablero[fila][j]=0;
        }
    }

    /*private void bajarFilaAnterior(int fila) {
        //implementando.....
        //TODO
    }*/
}

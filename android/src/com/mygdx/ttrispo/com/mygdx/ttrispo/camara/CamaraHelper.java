package com.mygdx.ttrispo.com.mygdx.ttrispo.camara;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.mygdx.ttrispo.AndroidLauncher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class CamaraHelper implements InterfazCamara {

    private String path, url;
    private int posicion;
    private byte[] bitMapAux;
    private boolean resultado1,resultado2;
    private final AndroidLauncher androidLauncher;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private StorageReference mStorageRef;
    private int numero;
    private ArrayList<File> imagenes;

    public CamaraHelper(AndroidLauncher androidLauncher){
        this.androidLauncher = androidLauncher;
        resultado1 = false;
        resultado2 = false;
        path = null;
        numero = 0;
        imagenes = new ArrayList<>();
        imagenes.add(null); //posicion 0
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    //SUBIR IMAGEN A LA BASE DE DATOS FIREBASE: NO TOCAR SI NO ERES EXPERTO
    public void subirImagen(int posicion){
        Uri file = Uri.fromFile(new File(getPath()));
        StorageReference riversRef = mStorageRef.child("images/"+posicion+".jpeg");
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                        setUrl(downloadUrl.toString());
                        metodoDefinitivoParaEnviarLaImagenConSuIDCorrespondienteDeLosCojones(getUrl());
                        //setResultado2(true);
                        System.out.println("La url legendaria es: " + downloadUrl + "\n\n\n\n\n");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(androidLauncher, "Error al subir el archivo, comprueba el acceso a internet", Toast.LENGTH_LONG);
                        System.out.println("ERROR AL SUBIR ARCHIVO. COMPRUEBA EL ACCESO A INTERNET");
                        //setResultado2(true);
                    }
                });
    }
    public boolean selectImage() {
        int permission = ActivityCompat.checkSelfPermission(androidLauncher, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //No tenemos permiso, por tanto se lo pedimos al usuario
            ActivityCompat.requestPermissions(
                    androidLauncher,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        final CharSequence[] options = {"Selfie", "Desde tu galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(androidLauncher);
        builder.setTitle("Sube tu foto!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Selfie")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //imagen temporal, luego se borra
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpeg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    androidLauncher.startActivityForResult(intent, 1);
                } else if (options[item].equals("Desde tu galeria")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    androidLauncher.startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        androidLauncher.runOnUiThread(new Runnable() {
            public void run() {
                builder.show();
            }
        });
        return true;
    }

    //GETTERS
    public byte[] getDatos(){
        return bitMapAux;
    }
    public boolean getResultado1(){
        return resultado1;
    }
    public boolean getResultado2(){
        return resultado2;
    }
    public String getUrl(){
        return this.url;
    }
    public int getPosicion(){
        return this.posicion;
    }

    private String getPath(){
        return this.path;
    }

    //SETTERS
    public void setDatos(byte[] bitMap){
        bitMapAux = bitMap;
    }
    public void setResultado1(boolean r1){
        resultado1 = r1;
    }
    public void setResultado2(boolean r2){
        resultado2 = r2;
    }
    public void setPath(String p){
        this.path = p;
    }
    public void setPosicion(int pos){
        this.posicion = pos;
    }

    private void setUrl(String u){
        this.url = u;
    }

    private void metodoDefinitivoParaEnviarLaImagenConSuIDCorrespondienteDeLosCojones(String url){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("bbdd").child(String.valueOf(getPosicion())).child("Imagen");
        myRef.setValue(url);
    }

    private File localFile = null;
    public void getImagenConPosicion(int i){
        numero = i;
        try {
            localFile = File.createTempFile("img" + i, ".jpeg");
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        StorageReference riversRef = mStorageRef.child("images/" + i + ".jpeg");



        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("Imagen " + numero + " : " + taskSnapshot.getBytesTransferred() + " / " + taskSnapshot.getTotalByteCount());
                        if(taskSnapshot.getTotalByteCount() == taskSnapshot.getBytesTransferred()){
                            cbImagenes(numero, localFile);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("ERROR EN LA IMAGEN DESCARGADA");
            }
        });
        //System.out.println("ARCHIVO: " + localFile.getAbsolutePath());
    }

    public void cbImagenes(int n, File lf){
        System.out.println("IMAGEN DESCARGADA " + n);
        setNumeroDeImagenes();
        System.out.println("cont imgs: " + getNumeroDeImagenes());
        imagenes.add(lf);
    }

    private void setNumeroDeImagenes(){
        numero++;
    }
    public int getNumeroDeImagenes(){
        return numero;
    }

    public ArrayList<File> getArrayImagenes(){
        return imagenes;
    }

    public byte[] convertirFileAbyte(File lf){
        byte[] byteArray = null;
            try{
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeFile(lf.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                byteArray = stream.toByteArray();

                //bitmap = BitmapFactory.decodeFile(lf.getAbsolutePath(), bitmapOptions);
/*
                ExifInterface ei = new ExifInterface(lf.getAbsolutePath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Bitmap rotatedBitmap = null;
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byteArray = stream.toByteArray();
 */

            } catch (Exception e) {
                e.printStackTrace();
            }

        return byteArray;
    }
/*
    //OPCIONES DE IMAGEN
    //rotar imagen bien
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    //imagenes redondas
    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r = 0;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    //imagenes cuadradas y centradas
    public static Bitmap getCropBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
 */
}

package cr.ac.itcr.tarea3;

/**
 * Created by Laurens on 20/04/2016.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import android.provider.ContactsContract.CommonDataKinds.Phone;


public class Main extends Activity {

    public static final int PICK_CONTACT_REQUEST = 1;     //Código del mensaje de envío
    private Uri contactUri;    //Uri de contenido global


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void initPickContacts(View v) {

        //Crear un intent para seleccionar un contacto del dispositivo

        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, PICK_CONTACT_REQUEST);
    }

    private void renderContact(Uri uri) {

        TextView contactName = (TextView) findViewById(R.id.contactName);
        TextView contactPhone = (TextView) findViewById(R.id.contactPhone);
        ImageView contactPic = (ImageView) findViewById(R.id.contactPic);


        contactName.setText(getName(uri));
        contactPhone.setText(getPhone(uri));
        contactPic.setImageBitmap(getPhoto(uri));
    }


    public void callContact(View v) {


        //Crea un intent para realizar la llamada

        Intent iCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                + getPhone(contactUri)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(iCall);

    }

    public void sendMessage(View v){

        SmsManager smsManager = SmsManager.getDefault();

        //Envia mensaje por defecto
        if(contactUri!=null) {
            smsManager.sendTextMessage(
                    getPhone(contactUri),
                    null,
                    "¡Estamos aprendiendo a Desarrollar en Android!",
                    null,
                    null);

            Toast.makeText(this, "Mensaje Enviado", Toast.LENGTH_LONG).show();
        }else
            Toast.makeText(this, "Selecciona un contacto primero", Toast.LENGTH_LONG).show();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {

                //Capturar el valor de la Uri

                contactUri = intent.getData();

                //Procesar la Uri

                renderContact(contactUri);
            }
        }
    }

    private String getPhone(Uri uri) {

        //Variables temporales para el id y el teléfono
        String id = null;
        String phone = null;


        //Obtener el _ID del contacto

        Cursor contactCursor = getContentResolver().query(
                uri,
                new String[]{Contacts._ID},
                null,
                null,
                null);


        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(0);
        }
        contactCursor.close();


        //Sentencia WHERE para especificar que solo deseamos
        //números de telefonía móvil

        String selectionArgs =
                Phone.CONTACT_ID + " = ? AND " +
                        Phone.TYPE+"= " +
                        Phone.TYPE_MOBILE;


        //Obtener el número telefónico
        Cursor phoneCursor = getContentResolver().query(
                Phone.CONTENT_URI,
                new String[] { Phone.NUMBER },
                selectionArgs,
                new String[] { id },
                null
        );
        if (phoneCursor.moveToFirst()) {
            phone = phoneCursor.getString(0);
        }
        phoneCursor.close();

        return phone;
    }

    private String getName(Uri uri) {

        String name = null;

        ContentResolver contentResolver = getContentResolver();


        //Consultar el nombre del contacto

        Cursor c = contentResolver.query(
                uri,
                new String[]{Contacts.DISPLAY_NAME},
                null,
                null,
                null);

        if(c.moveToFirst()){
            name = c.getString(0);
        }

        c.close();

        return name;
    }

    private Bitmap getPhoto(Uri uri) {
        Bitmap photo = null;
        String id = null;

        Cursor contactCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null);

        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(0);
        }
        contactCursor.close();


        //Usar el método de clase openContactPhotoInputStream()

        try {
            Uri contactUri = ContentUris.withAppendedId(
                    Contacts.CONTENT_URI,
                    Long.parseLong(id));

            InputStream input = Contacts.openContactPhotoInputStream(
                    getContentResolver(),
                    contactUri);

            if (input != null) {
                /*
                Dar formato tipo Bitmap a los bytes del BLOB
                correspondiente a la foto
                 */
                photo = BitmapFactory.decodeStream(input);
                input.close();
            }

        } catch (IOException iox) { }

        return photo;
    }
}

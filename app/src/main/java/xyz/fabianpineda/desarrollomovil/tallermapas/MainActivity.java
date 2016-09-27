package xyz.fabianpineda.desarrollomovil.tallermapas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {
    public static final int CODIGO_PETICION_PERMISO = 1;

    private LocationManager locationManager;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private boolean recibiendoUpdates;
    private AsyncHttpClient clienteHTTP;

    protected void mostrarGPS(Location ubicacion) {
        String proveedorUbicacion;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso denegado: GPS.", Toast.LENGTH_SHORT).show();
            return;
        }

        proveedorUbicacion = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : null;
        proveedorUbicacion = proveedorUbicacion == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ? LocationManager.NETWORK_PROVIDER : proveedorUbicacion;

        if (proveedorUbicacion == null) {
            mostrarSettingsGPS();
            return;
        }

        if (!map.isMyLocationEnabled()) {
            map.setMyLocationEnabled(true);
        }

        if (recibiendoUpdates) {
            if (ubicacion != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()), 18f));
            }
        } else {
            recibiendoUpdates = true;
            locationManager.requestLocationUpdates(proveedorUbicacion, 5000, 0, this);
            Toast.makeText(this, "Registrando listener de cambios de ubicación. Por favor espere.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    protected void mostrarHTTP() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso denegado: Internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        recibiendoUpdates = false;

        if (locationManager != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }

        /*
         * Si no está disponible en el futuro, la URL responde con el siguiente contenido JSON:
         *
         *      '{"lat": "10.370337", "long": "-75.465449"}'
         *
         * La ubicación representa una coordenada dentro de la Universidad.
         */
        String URL = "http://ubicacionutb.fabianpineda.xyz/loc.php";
        final AppCompatActivity refActivity = this;
        final GoogleMap gmap = map;
        Toast.makeText(refActivity, "Descargando JSON.", Toast.LENGTH_SHORT).show();

        AuxiliarHTTP.get(URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(refActivity, "JSON Recibido.", Toast.LENGTH_SHORT).show();

                try {
                    Toast.makeText(refActivity, "Operación exitosa.", Toast.LENGTH_SHORT).show();
                    gmap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            new LatLng(
                                Double.parseDouble((String) response.get("lat")),
                                Double.parseDouble((String) response.get("long"))
                            ),
                            18f
                        )
                    );

                    //Double.parseDouble((String) response.get("lat")),
                } catch (JSONException | NumberFormatException e) {
                    Toast.makeText(refActivity, "Error JSON.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void mostrarSettingsGPS() {
        final AppCompatActivity inst = this;
        final Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        new AlertDialog.Builder(this)
                .setTitle("Ubicación")
                .setMessage("La aplicación necesita acceso a su ubicación para continuar.\n\nDesea activar el servicio de ubicación en Settings?")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(inst, "Operación cancelada.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Abrir Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(settings);
                    }
                })
                .create().show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    protected void permisoConcedido(String permiso) {
        String formatoPermisoConcedido = "Permiso concedido%s.";

        switch (permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                Toast.makeText(this, String.format(formatoPermisoConcedido, ": GPS"), Toast.LENGTH_SHORT).show();
                mostrarGPS(null);
                break;
            case Manifest.permission.INTERNET:
                Toast.makeText(this, String.format(formatoPermisoConcedido, ": Internet"), Toast.LENGTH_SHORT).show();
                mostrarHTTP();
                break;
            default:
                // Ocurre cuando no se concedió ningun permiso. No debería ocurrir.
                Toast.makeText(this, String.format(formatoPermisoConcedido, ""), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    protected void permisoDenegado(String permiso) {
        String formatoErrorPermiso = "Permiso denegado%s.";

        if (permiso == null) {
            Toast.makeText(this, String.format(formatoErrorPermiso, ""), Toast.LENGTH_SHORT).show();
            return;
        }

        switch (permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                Toast.makeText(this, String.format(formatoErrorPermiso, ": GPS"), Toast.LENGTH_SHORT).show();
                break;
            case Manifest.permission.INTERNET:
                Toast.makeText(this, String.format(formatoErrorPermiso, ": Internet"), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, String.format(formatoErrorPermiso, ""), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODIGO_PETICION_PERMISO) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permisoConcedido(permissions[0]);
                } else {
                    permisoDenegado(permissions[0]);
                }
            } else {
                permisoDenegado(null);
            }
        }
    }

    protected void opcionGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mostrarGPS(null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PETICION_PERMISO);
        }
    }

    protected void opcionHTTP() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            mostrarHTTP();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, CODIGO_PETICION_PERMISO);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_gps:
                opcionGPS();
                break;
            case R.id.menu_http:
                opcionHTTP();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        recibiendoUpdates = false;

        clienteHTTP = new AsyncHttpClient();

        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.activity_main, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        recibiendoUpdates = false;

        if (locationManager != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            // ...
        } else {
            // ...
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (recibiendoUpdates) {
            mostrarGPS(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
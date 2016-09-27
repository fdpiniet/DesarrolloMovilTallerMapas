package xyz.fabianpineda.desarrollomovil.tallermapas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int CODIGO_PETICION_PERMISO = 1;

    private LocationManager locationManager;

    private GoogleMap map;
    private GoogleApiClient googlePlay;
    private boolean googlePlayAPIConectado;
    private SupportMapFragment mapFragment;

    protected void mostrarGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso denegado: GPS.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            mostrarSettingsGPS();
            return;
        }

        if (!map.isMyLocationEnabled()) {
            map.setMyLocationEnabled(true);
        }

        if (!googlePlayAPIConectado || !googlePlay.isConnected()) {
            Toast.makeText(this, "Reconectando a Google Play. Intente nuevamente.", Toast.LENGTH_SHORT).show();
            conectarGooglePlay();
            return;
        }

        Location loc = LocationServices.FusedLocationApi.getLastLocation(googlePlay);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));

    }

    protected void mostrarHTTP() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso denegado: Internet.", Toast.LENGTH_SHORT).show();
            return;
        }
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

        switch(permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                Toast.makeText(this, String.format(formatoPermisoConcedido, ": GPS"), Toast.LENGTH_SHORT).show();
                mostrarGPS();
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

        switch(permiso) {
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
            mostrarGPS();
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
        switch(item.getItemId()) {
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

        googlePlay = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
        .build();
        googlePlayAPIConectado = false;


        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.activity_main, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    protected void conectarGooglePlay() {
        if (googlePlay != null && !googlePlay.isConnected()) {
            googlePlay.connect();
        }
    }

    @Override
    protected void onStart() {
        conectarGooglePlay();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void desconectarGooglePlay() {
        if (googlePlay != null && googlePlay.isConnected()) {
            googlePlay.disconnect();
        }
    }

    @Override
    protected void onStop() {
        desconectarGooglePlay();
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
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Conectado a Google Play.", Toast.LENGTH_SHORT).show();
        googlePlayAPIConectado = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        googlePlayAPIConectado = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error conectando a Google Play.", Toast.LENGTH_SHORT).show();
        googlePlayAPIConectado = false;
    }
}
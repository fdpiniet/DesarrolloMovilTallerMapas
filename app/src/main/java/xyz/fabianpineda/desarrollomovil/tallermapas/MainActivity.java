package xyz.fabianpineda.desarrollomovil.tallermapas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int CODIGO_PETICION_PERMISO = 1;

    private boolean permisoGPS;
    private boolean permisoHTTP;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private boolean mapaListo;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapaListo = true;
        map = googleMap;

        LatLng ubicacionUTB = new LatLng(10.370337, -75.465449);
        LatLngBounds utbBounds = new LatLngBounds(new LatLng(10.368202, -75.466161), new LatLng(10.371140, -75.464468));
        CameraPosition inicial = (new CameraPosition.Builder()).target(utbBounds.getCenter()).zoom(18.5f).bearing(140f).build();

        /*
         * La parte checkSelfPermission, por mas innecesaria que sea, es un requisito o de Android,
         * o del IDE. "Necesita una manera de asegurarse que si se están comprobando permisos."
         */
        map.setMyLocationEnabled(permisoGPS || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMinZoomPreference(18.5f);
        map.setMaxZoomPreference(20.0f);
        map.setLatLngBoundsForCameraTarget(utbBounds);

        map.addMarker(new MarkerOptions().position(ubicacionUTB)).setVisible(true);

        map.moveCamera(CameraUpdateFactory.newCameraPosition(inicial));
    }

    protected void cargarMapa() {
        if (!mapaListo) {
            mapFragment.getMapAsync(this);
        }
    }

    public void permisoConcedido(String permiso) {
        switch(permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                permisoGPS = true;
                break;
            case Manifest.permission.INTERNET:
                permisoHTTP = true;
                break;
            default:
                // ...
                break;
        }

        cargarMapa();
    }

    public void permisoDenegado(String permiso) {
        if (permiso == null) {
            return;
        }

        switch(permiso) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                permisoGPS = false;
                break;
            case Manifest.permission.INTERNET:
                permisoHTTP = false;
                break;
            default:
                // ...
                break;
        }

        cargarMapa();
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

    public void pedirPermiso(String permiso, String mensaje) {
        Toast popup;

        if (ContextCompat.checkSelfPermission(this, permiso)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permiso)) {
                popup = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permiso}, CODIGO_PETICION_PERMISO);
            }
        } else {
            permisoConcedido(permiso);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_gps:
                if (!permisoGPS) {
                    pedirPermiso(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.antes_pedir_permisos));
                } else {
                    cargarMapa();
                }

                break;
            case R.id.menu_http:
                if (!permisoHTTP) {
                    pedirPermiso(Manifest.permission.INTERNET, getString(R.string.antes_pedir_permisos));
                } else {
                    cargarMapa();
                }

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

        permisoGPS = false;
        permisoHTTP = false;

        mapaListo = false;
        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.activity_main, mapFragment).commit();
    }

    @Override
    protected void onStart() {
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
}

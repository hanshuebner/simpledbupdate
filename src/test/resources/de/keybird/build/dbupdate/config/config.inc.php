<?php


/**
 * Globale Konfigurationsoptionen
 *
 * Optionen fur die Konfiguration der Anwendung
 */


// Array mit allen Konfigurationseinstellungen
$APP_CONF = array();

/** Wichtigste Configfiles */



// Datenbankspezifische Konfiguration
$APP_CONF['db_host'] = '127.0.0.1'; //Master DB
$APP_CONF['db_failover'] = '127.0.0.1'; //Failover-DB
$APP_CONF['db_user'] = 'user';
$APP_CONF['db_passwd'] = 'password';
$APP_CONF['db_database'] = 'database';
$APP_CONF['db_type'] = 'MySQL';

<?php


/**
 * Globale Konfigurationsoptionen
 *
 * Optionen fur die Konfiguration der Anwendung
 */


// Array mit allen Konfigurationseinstellungen
$APP_CONF = array();

/** Wichtigste Configfiles */

// Das Hauptverzeichnis der Anwendung
// Slash am Ende muss vorhanden sein
$APP_CONF['server_rootdir'] = '/home/entwickler/workspace/trunk-vk/output/';



// Datenbankspezifische Konfiguration
$APP_CONF['db_host'] = '127.0.0.1'; //Master DB
$APP_CONF['db_failover'] = '127.0.0.1'; //Failover-DB
$APP_CONF['db_user'] = 'root';
$APP_CONF['db_passwd'] = 'entwickler';
$APP_CONF['db_database'] = 'trunk-wild';
$APP_CONF['db_type'] = 'MySQL';
$APP_CONF['db_notify'] = 'patrick.schweizer@ecratum.de';

include($APP_CONF['server_rootdir'] ."classes/common/functionDbConnect.inc.php");
dbConnectConfig();

$res = mysql_query("SELECT * from srm_sysconfig");
while($row = mysql_fetch_array($res))
{
  $APP_CONF[$row['sConfigName']] = $row['sConfigValue'];
}


// Umfragesystem
$APP_CONF['survey_system'] = ''; // Leer oder PHPESP
$APP_CONF['survey_system_url'] = $APP_CONF['server_baseurl'].'/survey/';
$APP_CONF['survey_system_dbhost'] = 'localhost';
$APP_CONF['survey_system_dbname'] = 'srm_phpesp';
$APP_CONF['survey_system_dbuser'] = 'PHPESPBENUTZER';
$APP_CONF['survey_system_dbpassword'] = 'PHPESPPASSWORT';

/** Ab hier braucht normalerweise nix mehr veraendert zu werden */


// Das Verzeichnis fuer die API
$APP_CONF['server_apidir'] = $APP_CONF['server_rootdir'].'api';

// Das Verzeichnis fuer die compilierten Templates
$APP_CONF['server_templatesc_dir'] = $APP_CONF['server_rootdir'].'templates_c';

// Das Basis-Verzeichnis f&uuml;r den Dateiupload
$APP_CONF['server_uploaddir'] = $APP_CONF['server_rootdir'].'upload';

// Das Verzeichnis f&uuml;r den Upload f&uuml;r Importe
$APP_CONF['server_importdir'] = $APP_CONF['server_uploaddir'] . '/imports';

// Das Verzeichnis fuer den Upload jeglicher Dateien der Klasse File
$APP_CONF['server_filesdir'] = $APP_CONF['server_uploaddir'] . '/files';

// Das Verzeichnis f&uuml;r den Upload f&uuml;r Zertifikate
$APP_CONF['server_certificatedir'] = $APP_CONF['server_uploaddir'] . '/certificates';

// Das Verzeichnis f&uuml;r die Mailtemplates
$APP_CONF['mail_templatedir'] = $APP_CONF['server_rootdir'].'/mailtemplates';

// Systemsprache
$APP_CONF['system_language'] = 'de_DE';

// Sessiondaten
$APP_CONF['session_name'] = 'SID';
$APP_CONF['session_uid'] = '';
$APP_CONF['session_ip'] = '';
$APP_CONF['session_language'] = '';
$APP_CONF['session_type'] = 'cookie'; // cookie oder link

// Alle installierten Sprachen
$INSTALLED_LANGUAGES = array ('de_DE');
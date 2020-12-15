/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.switzerland.backend.sdk.config.ws.controller;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dpppt.switzerland.backend.sdk.config.ws.helper.IOS136InfoBoxHelper;
import org.dpppt.switzerland.backend.sdk.config.ws.model.*;
import org.dpppt.switzerland.backend.sdk.config.ws.poeditor.Messages;
import org.dpppt.switzerland.backend.sdk.config.ws.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ch.ubique.openapi.docannotations.Documentation;

/**
 * 
 * @CrossOrigin(origins = { "https://editor.swagger.io" })
 * @GetMapping(value = "") public @ResponseBody String hello() { return "Hello
 *                   from DP3T Config WS"; }
 * 
 * @CrossOrigin(origins = { "https://editor.swagger.io" })
 * @GetMapping(value = "/config") public @ResponseBody
 *                   ResponseEntity<ConfigResponse> getConfig(
 * @RequestParam String osversion,
 * @RequestParam String appversion,
 * @RequestParam String buildnr) { ConfigResponse config = new ConfigResponse();
 *               // For iOS 13.6 users with language DE show information about
 *               weekly // notification if
 *               (osversion.equals(IOS_VERSION_DE_WEEKLY_NOTIFCATION_INFO)) {
 *               setInfoTextForiOS136DE(config); }
 * 
 *               // if we have testflight builds suggest to switch to store
 *               version if (TESTFLIGHT_VERSIONS.contains(buildnr)) { config =
 *               testFlightUpdate(); }
 * 
 *               // Build nr of the initial iOS pilot test app. Contains bug,
 *               that factors are // not used correctly in contact calculations.
 *               Set factorHigh to 0.0 for // improving the calculation. if
 *               (buildnr.equals("ios-200524.1316.87")) {
 *               config.getiOSGaenSdkConfig().setFactorHigh(0.0d); } return
 *               ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(config);
 *               }
 * 
 * @Documentation(description = "Infobox testing endpoint", responses = {"200 =>
 *                            Infobox in all languages"})
 * @CrossOrigin(origins = { "https://editor.swagger.io" })
 * @GetMapping(value = "/testinfobox/config") public @ResponseBody
 *                   ResponseEntity<ConfigResponse> getGhettoboxConfig(
 * @RequestParam String osversion,
 * @RequestParam String appversion,
 * @RequestParam String buildnr) { ConfigResponse body =
 *               mockConfigResponseWithInfoBox(); return
 *               ResponseEntity.ok(body); }
 * 
 */

@Controller
@RequestMapping("/v1")
public class GaenConfigController {

	private static final String IOS_VERSION_DE_WEEKLY_NOTIFCATION_INFO = "ios13.6";
	private static final List<String> TESTFLIGHT_VERSIONS = List.of("ios-200619.2333.175", "ios-200612.2347.141",
			"ios-200528.2230.100", "ios-200524.1316.87", "ios-200521.2320.79");
	private static final String IOS_VERSION_13_7 = "ios13.7";
	private static final String IOS_VERSION_14 = "ios14.0";
	private static final Version APP_VERSION_1_0_9 = new Version("ios-1.0.9");
	private static final Version IOS_APP_VERSION_1_1_2 = new Version("ios-1.1.2");

	private static final Set<String> ALL_CANTONS_AND_LIECHTENSTEIN = Set.of("Graubünden", "Zürich", "Bern", "Luzern",
			"Uri", "Schwyz", "Obwalden", "Nidwalden", "Glarus", "Zug", "Freiburg", "Solothurn", "Basel_Stadt",
			"Basel_Landschaft", "Schafhausen", "Appenzell_Ausserrhoden", "Appenzell_Innerrhoden", "St_Gallen", "Aargau",
			"Thurgau", "Tessin", "Waadt", "Wallis", "Neuenburg", "Genf", "Jura", "Fürstentum_Liechtenstein");

	private static final Map<String, String> TEST_LOCATIONS = Map.ofEntries(
			Map.entry("Graubünden",
					"https://www.gr.ch/DE/institutionen/verwaltung/djsg/ga/coronavirus/info/Seiten/Start.aspx"),
			Map.entry("Zürich", "https://www.zh.ch/de/gesundheit/coronavirus.html"),
			Map.entry("Bern", "http://www.be.ch/corona"),
			Map.entry("Luzern", "https://gesundheit.lu.ch/themen/Humanmedizin/Infektionskrankheiten/Coronavirus"),
			Map.entry("Uri", "https://www.ur.ch/themen/2962"),
			Map.entry("Schwyz",
					"https://www.sz.ch/behoerden/information-medien/medienmitteilungen/coronavirus.html/72-416-412-1379-6948"),
			Map.entry("Obwalden", "https://www.ow.ch/de/verwaltung/dienstleistungen/?dienst_id=5962"),
			Map.entry("Nidwalden", "https://www.nw.ch/gesundheitsamtdienste/6044"),
			Map.entry("Glarus",
					"https://www.gl.ch/verwaltung/finanzen-und-gesundheit/gesundheit/coronavirus.html/4817"),
			Map.entry("Zug", "https://www.zg.ch/behoerden/gesundheitsdirektion/amt-fuer-gesundheit/corona"),
			Map.entry("Freiburg", "https://www.fr.ch/de/gesundheit/covid-19/coronavirus-aktuelle-informationen"),
			Map.entry("Solothurn", "https://corona.so.ch/"), Map.entry("Basel_Stadt", "https://www.coronavirus.bs.ch/"),
			Map.entry("Basel_Landschaft",
					"https://www.baselland.ch/politik-und-behorden/direktionen/volkswirtschafts-und-gesundheitsdirektion/amt-fur-gesundheit/medizinische-dienste/kantonsarztlicher-dienst/aktuelles"),
			Map.entry("Schafhausen",
					"https://sh.ch/CMS/Webseite/Kanton-Schaffhausen/Beh-rde/Verwaltung/Departement-des-Innern/Gesundheitsamt-2954701-DE.html"),
			Map.entry("Appenzell_Ausserrhoden",
					"https://www.ar.ch/verwaltung/departement-gesundheit-und-soziales/amt-fuer-gesundheit/informationsseite-coronavirus/"),
			Map.entry("Appenzell_Innerrhoden",
					"https://www.ai.ch/themen/gesundheit-alter-und-soziales/gesundheitsfoerderung-und-praevention/uebertragbare-krankheiten/coronavirus"),
			Map.entry("St_Gallen", "https://www.sg.ch/tools/informationen-coronavirus.html"),
			Map.entry("Aargau", "https://www.ag.ch/de/themen_1/coronavirus_2/coronavirus.jsp"),
			Map.entry("Thurgau", "https://www.tg.ch/news/fachdossier-coronavirus.html/10552"),
			Map.entry("Tessin", "https://www4.ti.ch/dss/dsp/covid19/home/"),
			Map.entry("Waadt", "https://www.vd.ch/toutes-les-actualites/hotline-et-informations-sur-le-coronavirus/"),
			Map.entry("Wallis", "https://www.vs.ch/de/web/coronavirus"),
			Map.entry("Neuenburg",
					"https://www.ne.ch/autorites/DFS/SCSP/medecin-cantonal/maladies-vaccinations/Pages/Coronavirus.aspx"),
			Map.entry("Genf", "https://www.ge.ch/covid-19-se-proteger-prevenir-nouvelle-vague"),
			Map.entry("Jura",
					"https://www.jura.ch/fr/Autorites/Coronavirus/Accueil/Coronavirus-Informations-officielles-a-la-population-jurassienne.html"),
			Map.entry("Fürstentum_Liechtenstein", "https://www.llv.li/inhalt/118724/amtsstellen/coronavirus"));

	private static final Logger logger = LoggerFactory.getLogger(GaenConfigController.class);

	private static Messages messages;

	public GaenConfigController(Messages messages) {
		this.messages = messages;
	}

	@Documentation(description = "Echo endpoint", responses = { "200 => Hello from DP3T Config WS" })
	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "")
	public @ResponseBody String hello() {
		return "Hello from DP3T Config WS";
	}

	@Documentation(description = "Read latest configuration and messages, depending on the version of the phone and the"
			+ " app.", responses = {
					"200 => ConfigResponse structure with eventual notifications and epidemic parameters" })
	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/config")
	public @ResponseBody ResponseEntity<ConfigResponse> getConfig(
			@Documentation(description = "Version of the App installed", example = "ios-1.0.7") @RequestParam String appversion,
			@Documentation(description = "Version of the OS", example = "ios13.6") @RequestParam String osversion,
			@Documentation(description = "Build number of the app", example = "ios-200619.2333.175") @RequestParam String buildnr) {
		ConfigResponse config = new ConfigResponse();
		config.setWhatToDoPositiveTestTexts(whatToDoPositiveTestTexts(messages));
		config.setTestLocations(TEST_LOCATIONS);

		// For iOS 13.6 users show information about weekly notification
		if (osversion.startsWith(IOS_VERSION_DE_WEEKLY_NOTIFCATION_INFO)) {
			IOS136InfoBoxHelper.setInfoTextForiOS136(config);
		}

		// if we have testflight builds suggest to switch to store version
		if (TESTFLIGHT_VERSIONS.contains(buildnr)) {
			config = testFlightUpdate();
		}

		// Build nr of the initial iOS pilot test app. Contains bug, that factors are
		// not used correctly in contact calculations. Set factorHigh to 0.0 for
		// improving the calculation.
		if (buildnr.equals("ios-200524.1316.87")) {
			config.getiOSGaenSdkConfig().setFactorHigh(0.0d);
		}

		// Check for old app Versions, iOS only
		Version userAppVersion = new Version(appversion);
		if (userAppVersion.isIOS() && APP_VERSION_1_0_9.isLargerVersionThan(userAppVersion)) {
			config = generalUpdateRelease(true);
		}

		// Work around a limitation of SwissCovid 1.1.2 on iOS which requires an InfoBox
		// to be set.
		// For this specific version, move the text above the "Enter CovidCode" button,
		// below into the
		// InfoBox if no other InfoBox is present.
		if (userAppVersion.isIOS() && IOS_APP_VERSION_1_1_2.isSameVersionAs(userAppVersion)) {
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getDe());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getFr());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getIt());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getEn());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getPt());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getEs());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getSq());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getBs());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getHr());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getSr());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getRm());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getTr());
			moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(config.getWhatToDoPositiveTestTexts().getTi());
		}

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(config);
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/testinfobox/config")
	public @ResponseBody ResponseEntity<ConfigResponse> getGhettoboxConfig(
			@Documentation(description = "Version of the App installed", example = "ios-1.0.7") @RequestParam String appversion,
			@Documentation(description = "Version of the OS", example = "ios13.6") @RequestParam String osversion,
			@Documentation(description = "Build number of the app", example = "ios-200619.2333.175") @RequestParam String buildnr) {
		ConfigResponse body = mockConfigResponseWithInfoBox();
		return ResponseEntity.ok(body);
	}

	private ConfigResponse testFlightUpdate() {
		ConfigResponse configResponse = new ConfigResponse();
		String iosURL = "https://apps.apple.com/ch/app/id1509275381";
		InfoBox infoBoxde = new InfoBox();
		infoBoxde.setMsg("Die App wird in Zukunft nicht mehr über Testflight verfügbar sein.");
		infoBoxde.setTitle("App-Update im App Store");
		infoBoxde.setUrlTitle("Aktualisieren");
		infoBoxde.setUrl(iosURL);
		InfoBox infoBoxfr = new InfoBox();
		infoBoxfr.setMsg("L'application ne sera plus disponible sur TestFlight.");
		infoBoxfr.setTitle("Mise à jour dans l'App Store");
		infoBoxfr.setUrlTitle("Mettre à jour");
		infoBoxfr.setUrl(iosURL);
		InfoBox infoBoxit = new InfoBox();
		infoBoxit.setMsg("In futuro l'app non sarà più disponibile tramite Testflight.");
		infoBoxit.setTitle("Aggiornamento dell'app nell'App Store");
		infoBoxit.setUrlTitle("Aggiorna");
		infoBoxit.setUrl(iosURL);
		InfoBox infoBoxen = new InfoBox();
		infoBoxen.setMsg("The app will no longer be available via Testflight.");
		infoBoxen.setTitle("App update in the App Store");
		infoBoxen.setUrlTitle("Update");
		infoBoxen.setUrl(iosURL);
		InfoBox infoBoxpt = new InfoBox();
		infoBoxpt.setMsg("Futuramente, a app deixará de estar disponível na Testflight.");
		infoBoxpt.setTitle("Atualização da app na App Store");
		infoBoxpt.setUrlTitle("Atualizar");
		infoBoxpt.setUrl(iosURL);
		InfoBox infoBoxes = new InfoBox();
		infoBoxes.setMsg("En el futuro la aplicación dejará de estar disponible a través de Textflight.");
		infoBoxes.setTitle("Actualización de la app en el App Store");
		infoBoxes.setUrlTitle("Actualizar");
		infoBoxes.setUrl(iosURL);
		InfoBox infoBoxsq = new InfoBox();
		infoBoxsq.setMsg("Në të ardhmen aplikacioni nuk do të jetë më i disponueshëm përmes Testflight.");
		infoBoxsq.setTitle("Update i aplikacionit në App Store");
		infoBoxsq.setUrlTitle("Përditësimi");
		infoBoxsq.setUrl(iosURL);
		InfoBox infoBoxbs = new InfoBox();
		infoBoxbs.setMsg("Aplikacija ubuduće više neće biti dostupna preko Testflight-a.");
		infoBoxbs.setTitle("Ažuriranje aplikacije u trgovini aplikacijama App Store");
		infoBoxbs.setUrlTitle("Ažuriraj");
		infoBoxbs.setUrl(iosURL);
		InfoBox infoBoxhr = new InfoBox();
		infoBoxhr.setMsg("Aplikacija ubuduće više neće biti dostupna preko Testflight-a.");
		infoBoxhr.setTitle("Ažuriranje aplikacije u trgovini aplikacijama App Store");
		infoBoxhr.setUrlTitle("Ažuriraj");
		infoBoxhr.setUrl(iosURL);
		InfoBox infoBoxrm = new InfoBox();
		infoBoxrm.setMsg("En il futur na vegn l'app betg pli ad esser disponibla via Testflight.");
		infoBoxrm.setTitle("Actualisaziun da l'app en l'App Store");
		infoBoxrm.setUrlTitle("Actualisar");
		infoBoxrm.setUrl(iosURL);
		InfoBox infoBoxsr = new InfoBox();
		infoBoxsr.setMsg("Aplikacija ubuduće više neće biti dostupna preko Testflight-a.");
		infoBoxsr.setTitle("Ažuriranje aplikacije u trgovini aplikacijama App Store");
		infoBoxsr.setUrlTitle("Ažuriraj");
		infoBoxsr.setUrl(iosURL);

		InfoBoxCollection collection = new InfoBoxCollection();
		collection.setDeInfoBox(infoBoxde);
		collection.setEnInfoBox(infoBoxen);
		collection.setFrInfoBox(infoBoxfr);
		collection.setItInfoBox(infoBoxit);
		collection.setPtInfoBox(infoBoxpt);
		collection.setEsInfoBox(infoBoxes);
		collection.setSqInfoBox(infoBoxsq);
		collection.setHrInfoBox(infoBoxhr);
		collection.setBsInfoBox(infoBoxbs);
		collection.setRmInfoBox(infoBoxrm);
		collection.setSrInfoBox(infoBoxsr);
		configResponse.setInfoBox(collection);

		return configResponse;

	}

	private ConfigResponse generalUpdateRelease(boolean isIos) {
		ConfigResponse configResponse = new ConfigResponse();
		String appstoreUrl = isIos ? "https://apps.apple.com/ch/app/id1509275381"
				: "https://play.google.com/store/apps/details?id=ch.admin.bag.dp3t";

		String store = isIos ? "App Store" : "Play Store";
		String storeFr = isIos ? "l'App Store" : "le Play Store";
		String storeRm = isIos ? "da l'App Store" : "dal Play Store";

		InfoBox infoBoxde = new InfoBox();
		infoBoxde.setMsg(
				"Es ist eine neuere Version von SwissCovid verfügbar. Um die bestmögliche Funktionsweise der App zu erhalten, laden Sie die neuste Version vom "
						+ store);
		infoBoxde.setTitle("App-Update verfügbar");
		infoBoxde.setUrlTitle("Aktualisieren");
		infoBoxde.setUrl(appstoreUrl);
		infoBoxde.setIsDismissible(false);

		InfoBox infoBoxfr = new InfoBox();
		infoBoxfr.setMsg(
				"Une nouvelle version de SwissCovid est disponible. Afin que l'application fonctionne au mieux, téléchargez la dernière version sur "
						+ storeFr);
		infoBoxfr.setTitle("Mise à jour disponible");
		infoBoxfr.setUrlTitle("Mettre à jour");
		infoBoxfr.setUrl(appstoreUrl);
		infoBoxfr.setIsDismissible(false);

		InfoBox infoBoxit = new InfoBox();
		infoBoxit.setMsg(
				"È disponibile una versione più recente di SwissCovid. Per ottimizzare la funzionalità dell'app, scarica l'ultima versione da "
						+ store);
		infoBoxit.setTitle("È disponibile un aggiornamento dell'app");
		infoBoxit.setUrlTitle("Aggiorna");
		infoBoxit.setUrl(appstoreUrl);
		infoBoxit.setIsDismissible(false);

		InfoBox infoBoxen = new InfoBox();
		infoBoxen.setMsg(
				"An updated version of SwissCovid is available. To guarantee the app works as well as possible, download the latest version from the "
						+ store);
		infoBoxen.setTitle("App update available");
		infoBoxen.setUrlTitle("Update");
		infoBoxen.setUrl(appstoreUrl);
		infoBoxen.setIsDismissible(false);

		InfoBox infoBoxpt = new InfoBox();
		infoBoxpt.setMsg(
				"Está disponível uma nova versão da SwissCovid. Para que a app trabalhe com toda a eficiência, carregue a versão mais recente a partir da "
						+ store);
		infoBoxpt.setTitle("Atualização da app disponível");
		infoBoxpt.setUrlTitle("Atualizar");
		infoBoxpt.setUrl(appstoreUrl);
		infoBoxpt.setIsDismissible(false);

		InfoBox infoBoxes = new InfoBox();
		infoBoxes.setMsg(
				"Hay una nueva versión de SwissCovid disponible. Para garantizar el mejor funcionamiento posible, descargue siempre la versión más nueva en el "
						+ store);
		infoBoxes.setTitle("Actualización de la app disponible");
		infoBoxes.setUrlTitle("Actualizar");
		infoBoxes.setUrl(appstoreUrl);
		infoBoxes.setIsDismissible(false);

		InfoBox infoBoxsq = new InfoBox();
		infoBoxsq.setMsg(
				"Është i disponueshëm një version i ri nga SwissCovid. Për të marrë mënyrën më të mirë të mundshme të funksionit të aplikacionit, ngarkoni versionin më të ri nga "
						+ store);
		infoBoxsq.setTitle("Update i aplikacionit i disponueshëm");
		infoBoxsq.setUrlTitle("Përditësimi");
		infoBoxsq.setUrl(appstoreUrl);
		infoBoxsq.setIsDismissible(false);

		InfoBox infoBoxbs = new InfoBox();
		infoBoxbs.setMsg(
				"Dostupna je novija verzija aplikacije SwissCovid. Da biste održavali najbolju moguću funkcionalnost aplikacije, preuzmite najnoviju verziju iz trgovine aplikacijama "
						+ store);
		infoBoxbs.setTitle("Dostupno ažuriranje aplikacije");
		infoBoxbs.setUrlTitle("Ažuriraj");
		infoBoxbs.setUrl(appstoreUrl);
		infoBoxbs.setIsDismissible(false);

		InfoBox infoBoxhr = new InfoBox();
		infoBoxhr.setMsg(
				"Dostupna je novija verzija aplikacije SwissCovid. Da biste održavali najbolju moguću funkcionalnost aplikacije, preuzmite najnoviju verziju iz trgovine aplikacijama "
						+ store);
		infoBoxhr.setTitle("Dostupno ažuriranje aplikacije");
		infoBoxhr.setUrlTitle("Ažuriraj");
		infoBoxhr.setUrl(appstoreUrl);
		infoBoxhr.setIsDismissible(false);

		InfoBox infoBoxrm = new InfoBox();
		infoBoxrm.setMsg("Ina versiun pli nova da SwissCovid è disponibla. Chargiai giu l'ultima versiun " + storeRm
				+ ", per che l'app funcziunia il meglier pussaivel.");
		infoBoxrm.setTitle("Actualisaziun da l'app è disponibla");
		infoBoxrm.setUrlTitle("Actualisar");
		infoBoxrm.setUrl(appstoreUrl);
		infoBoxrm.setIsDismissible(false);

		InfoBox infoBoxsr = new InfoBox();
		infoBoxsr.setMsg(
				"Dostupna je novija verzija aplikacije SwissCovid. Da biste održavali najbolju moguću funkcionalnost aplikacije, preuzmite najnoviju verziju iz trgovine aplikacijama "
						+ store);
		infoBoxsr.setTitle("Dostupno ažuriranje aplikacije");
		infoBoxsr.setUrlTitle("Ažuriraj");
		infoBoxsr.setUrl(appstoreUrl);
		infoBoxsr.setIsDismissible(false);

		InfoBox infoBoxtr = new InfoBox();
		infoBoxtr.setMsg(
				"SwissCovid uygulamasının yeni sürümü bulunuyor. Uygulamayı en iyi şekilde kullanabilmek için AppStore'dan uygulamanın son sürümünü yükleyin.");
		infoBoxtr.setTitle("Güncelleştirme mevcut");
		infoBoxtr.setUrlTitle("Güncelle");
		infoBoxtr.setUrl(appstoreUrl);
		infoBoxtr.setIsDismissible(false);

		InfoBox infoBoxti = new InfoBox();
		infoBoxti.setMsg(
				"ሓድሽ ቨርዝዮን ናይ SwissCovid ተቐሪቡ። ዝበለጸ ኣሰራርሓ ናይቲ ኤፕ መታን ክወሃበኩም፣ እቲ ሓድሽ ቨርዝዮን ካብ AppStore ብዳውንሎድ ኣምጽኡ ኢኹም።");
		infoBoxti.setTitle("ሓድሽ ኤፕ-ኣፕደይት ኣሎ");
		infoBoxti.setUrlTitle("ምምሕዳስ");
		infoBoxti.setUrl(appstoreUrl);
		infoBoxti.setIsDismissible(false);

		InfoBoxCollection collection = new InfoBoxCollection();
		collection.setDeInfoBox(infoBoxde);
		collection.setEnInfoBox(infoBoxen);
		collection.setFrInfoBox(infoBoxfr);
		collection.setItInfoBox(infoBoxit);
		collection.setPtInfoBox(infoBoxpt);
		collection.setEsInfoBox(infoBoxes);
		collection.setSqInfoBox(infoBoxsq);
		collection.setHrInfoBox(infoBoxhr);
		collection.setBsInfoBox(infoBoxbs);
		collection.setRmInfoBox(infoBoxrm);
		collection.setSrInfoBox(infoBoxsr);
		collection.setTiInfobox(infoBoxti);
		collection.setTrInfobox(infoBoxtr);

		configResponse.setInfoBox(collection);

		return configResponse;
	}

	private ConfigResponse mockConfigResponseWithInfoBox() {
		ConfigResponse configResponse = new ConfigResponse();

		InfoBox infoBoxde = new InfoBox();
		infoBoxde.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz DE");
		infoBoxde.setTitle("Hinweis DE");
		infoBoxde.setUrlTitle("Und ein externer Link DE");
		infoBoxde.setUrl("https://www.bag.admin.ch/bag/de/home.html");
		InfoBox infoBoxfr = new InfoBox();
		infoBoxfr.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz FR");
		infoBoxfr.setTitle("Hinweis FR");
		infoBoxfr.setUrlTitle("Und ein externer Link FR");
		infoBoxfr.setUrl("https://www.bag.admin.ch/bag/fr/home.html");
		InfoBox infoBoxit = new InfoBox();
		infoBoxit.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz IT");
		infoBoxit.setTitle("Hinweis IT");
		infoBoxit.setUrlTitle("Und ein externer Link IT");
		infoBoxit.setUrl("https://www.bag.admin.ch/bag/it/home.html");
		InfoBox infoBoxen = new InfoBox();
		infoBoxen.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz EN");
		infoBoxen.setTitle("Hinweis EN");
		infoBoxen.setUrlTitle("Und ein externer Link EN");
		infoBoxen.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxpt = new InfoBox();
		infoBoxpt.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz PT");
		infoBoxpt.setTitle("Hinweis PT");
		infoBoxpt.setUrlTitle("Und ein externer Link PT");
		infoBoxpt.setUrl("https://www.bag.admin.ch/bag/pt/home.html");
		InfoBox infoBoxes = new InfoBox();
		infoBoxes.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz ES");
		infoBoxes.setTitle("Hinweis ES");
		infoBoxes.setUrlTitle("Und ein externer Link ES");
		infoBoxes.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxsq = new InfoBox();
		infoBoxsq.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz SQ");
		infoBoxsq.setTitle("Hinweis SQ");
		infoBoxsq.setUrlTitle("Und ein externer Link SQ");
		infoBoxsq.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxbs = new InfoBox();
		infoBoxbs.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz BS");
		infoBoxbs.setTitle("Hinweis BS");
		infoBoxbs.setUrlTitle("Und ein externer Link BS");
		infoBoxbs.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxhr = new InfoBox();
		infoBoxhr.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz HR");
		infoBoxhr.setTitle("Hinweis HR");
		infoBoxhr.setUrlTitle("Und ein externer Link HR");
		infoBoxhr.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxrm = new InfoBox();
		infoBoxrm.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz RM");
		infoBoxrm.setTitle("Hinweis RM");
		infoBoxrm.setUrlTitle("Und ein externer Link RM");
		infoBoxrm.setUrl("https://www.bag.admin.ch/bag/en/home.html");
		InfoBox infoBoxsr = new InfoBox();
		infoBoxsr.setMsg("Hier steht ein Text. Das kann ein Hinweis sein. Je länger umso mehr Platz SR");
		infoBoxsr.setTitle("Hinweis SR");
		infoBoxsr.setUrlTitle("Und ein externer Link SR");
		infoBoxsr.setUrl("https://www.bag.admin.ch/bag/en/home.html");

		InfoBoxCollection collection = new InfoBoxCollection();
		collection.setDeInfoBox(infoBoxde);
		collection.setEnInfoBox(infoBoxen);
		collection.setFrInfoBox(infoBoxfr);
		collection.setItInfoBox(infoBoxit);
		collection.setPtInfoBox(infoBoxpt);
		collection.setEsInfoBox(infoBoxes);
		collection.setSqInfoBox(infoBoxsq);
		collection.setHrInfoBox(infoBoxhr);
		collection.setBsInfoBox(infoBoxbs);
		collection.setRmInfoBox(infoBoxrm);
		collection.setSrInfoBox(infoBoxsr);
		configResponse.setInfoBox(collection);

		return configResponse;
	}

	public ConfigResponse mockConfigResponseWithForceUpdate() {
		ConfigResponse configResponse = new ConfigResponse();
		configResponse.setForceUpdate(true);
		return configResponse;
	}

	private void moveEnterCovidcodeBoxTextToInfoBoxIfNecessary(WhatToDoPositiveTestTexts texts) {
		if (texts.getInfoBox() == null) {
			texts.setInfoBox(new InfoBox() {
				{
					setTitle("");
					setMsg(texts.getEnterCovidcodeBoxText());
					setIsDismissible(false);
				}
			});
			texts.setEnterCovidcodeBoxText("");
		}
	}

	private WhatToDoPositiveTestTextsCollection whatToDoPositiveTestTexts(Messages messages) {
		return new WhatToDoPositiveTestTextsCollection() {
			{
				setDe(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("de")));
				setFr(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("fr")));
				setIt(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("it")));
				setEn(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("en")));
				setPt(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("pt")));
				setEs(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("es")));
				setSq(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("sq")));
				setBs(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("bs")));
				setHr(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("hr")));
				setSr(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("sr")));
				setRm(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("rm")));
				setTr(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("tr")));
				setTi(getWhatToDoPositiveTestText(messages, Locale.forLanguageTag("ti")));
			}
		};
	}

	private WhatToDoPositiveTestTexts getWhatToDoPositiveTestText(Messages messages, Locale locale) {
		return new WhatToDoPositiveTestTexts() {
			{
				setEnterCovidcodeBoxSupertitle(messages.getMessage("inform_detail_box_subtitle", locale));
				setEnterCovidcodeBoxTitle(messages.getMessage("inform_detail_box_title", locale));
				setEnterCovidcodeBoxText(messages.getMessage("inform_detail_box_text", locale));
				setEnterCovidcodeBoxButtonTitle(messages.getMessage("inform_detail_box_button", locale));

				setInfoBox(null); // no infobox needed at the moment

				setFaqEntries(Arrays.asList(new FaqEntry() {
					{
						setTitle(messages.getMessage("inform_detail_faq1_title", locale));
						setText(messages.getMessage("inform_detail_faq1_text", locale));
						setLinkTitle(messages.getMessage("infoline_coronavirus_number", locale));
						setLinkUrl(
								"tel://" + messages.getMessage("infoline_coronavirus_number", locale).replace(" ", ""));
						setIconAndroid("ic_verified_user");
						setIconIos("ic-verified-user");
					}
				}, new FaqEntry() {
					{
						setTitle(messages.getMessage("inform_detail_faq2_title", locale));
						setText(messages.getMessage("inform_detail_faq2_text", locale));
						setIconAndroid("ic_key");
						setIconIos("ic-key");
					}
				}, new FaqEntry() {
					{
						setTitle(messages.getMessage("inform_detail_faq3_title", locale));
						setText(messages.getMessage("inform_detail_faq3_text", locale));
						setIconAndroid("ic_person");
						setIconIos("ic-user");
					}
				}));
			}
		};
	}
}

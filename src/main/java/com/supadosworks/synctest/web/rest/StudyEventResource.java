package com.supadosworks.synctest.web.rest;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.oc.connection.OcConnect;
import com.oc.model.StudyEventHelper;
import com.oc.model.StudyEventOC;
import com.supadosworks.synctest.domain.StudyEvent;
import com.supadosworks.synctest.repository.StudyEventRepository;

/**
 * REST controller for managing StudyEvent.
 */
@RestController
@RequestMapping("/api")
public class StudyEventResource {

	@RequestMapping(value = "/oc/icsStudyEvents/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void getICSFile(@PathVariable Long id, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		StudyEvent se = studyEventRepository.findOne(id);
		if (se != null
				&& (se.getDate_start() != null || se.getDate_end() != null)) {
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			StringBuilder sb = new StringBuilder();
			sb.append("BEGIN:VCALENDAR\n");
			sb.append("VERSION:2.0\n");

			sb.append("BEGIN:VEVENT\n");
			sb.append("CLASS:PUBLIC\n");
			sb.append("DESCRIPTION:" + se.getLabel() + "\n");
			String ds = null;
			ds = df.format(se.getDate_start());
			sb.append("DTSTART;VALUE=DATE:" + ds + "\n");
			String de = null;
			de = df.format(se.getDate_end());
			sb.append("DTEND;VALUE=DATE:" + de + "\n");
			sb.append("LOCATION:" + se.getLocation() + "\n");
			sb.append("SUMMARY;LANGUAGE=en-us:" + se.getId() + " - "
					+ se.getName() + "\n");
			sb.append("TRANSP:TRANSPARENT\n");
			sb.append("END:VEVENT\n");

			sb.append("END:VCALENDAR");

			response.setContentLength(sb.length());

			response.setHeader("Content-Type", "text/Calendar");
			response.setHeader("Content-Disposition",
					"attachment; filename=StudyEvent-" + + se.getId() + ".ics");

			// get output stream of the response
			OutputStream outStream = response.getOutputStream();

			// write bytes read from the input stream into the output stream

			outStream.write(sb.toString().getBytes());

			outStream.close();
		}

		// DateFormat df = new SimpleDateFormat("yyyyMMdd");
		//
		// StringBuilder sb = new StringBuilder();
		// sb.append("BEGIN:VCALENDAR\n");
		// sb.append("VERSION:2.0\n");
		// for (StudyEvent se : studyEventRepository.findAll()) {
		// if (se.getDate_end() == null || se.getDate_start() == null) {
		// continue;
		// }
		// sb.append("BEGIN:VEVENT\n");
		// sb.append("CLASS:PUBLIC\n");
		// sb.append("DESCRIPTION:" + se.getLabel() + "\n");
		// String ds = null;
		// ds = df.format(se.getDate_start());
		// sb.append("DTSTART;VALUE=DATE:" + ds + "\n");
		// String de = null;
		// de = df.format(se.getDate_end());
		// sb.append("DTEND;VALUE=DATE:" + de + "\n");
		// sb.append("LOCATION:" + se.getLocation() + "\n");
		// sb.append("SUMMARY;LANGUAGE=en-us:" + se.getName() + "\n");
		// sb.append("TRANSP:TRANSPARENT\n");
		// sb.append("END:VEVENT\n");
		// }
		//
		// sb.append("END:VCALENDAR");
		//
		// response.setContentLength(sb.length());
		//
		// response.setHeader("Content-Type", "text/Calendar");
		// response.setHeader("Content-Disposition",
		// "attachment; filename=StudyEvent.ics");
		//
		// // get output stream of the response
		// OutputStream outStream = response.getOutputStream();
		//
		// // write bytes read from the input stream into the output stream
		//
		// outStream.write(sb.toString().getBytes());
		//
		// outStream.close();
	}

	@RequestMapping(value = "/oc/studyEvents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<StudyEventOC> getDataFromOC() {
		List<StudyEventOC> r = new ArrayList<StudyEventOC>();
		StringBuilder sb = new StringBuilder();
		sb.append(" select");
		sb.append("	se.study_event_id as id,");
		sb.append(" sed.name as name,");
		sb.append(" ss.label as label,");
		sb.append(" ses.name as event_status,");
		sb.append(" s.name as status,");
		sb.append(" se.date_start,");
		sb.append(" se.date_end,");
		sb.append(" se.owner_id,");
		sb.append(" se.location,");
		sb.append(" se.start_time_flag,");
		sb.append(" se.end_time_flag");
		sb.append(" from study_event se");
		sb.append(" left join subject_event_status ses on se.subject_event_status_id = ses.subject_event_status_id");
		sb.append(" left join study_event_definition sed on se.study_event_definition_id = sed.study_event_definition_id");
		sb.append(" left join study_subject ss on se.study_subject_id = ss.study_subject_id");
		sb.append(" left join status s on se.status_id = s.status_id;");
		try {
			OcConnect oc = OcConnect.getConnection(sb.toString());
			ResultSet rs = oc.rs;
			while (rs.next()) {
				r.add(StudyEventOC.getDataset(rs));
			}
			OcConnect.close(oc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	@RequestMapping(value = "/studyEvents", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void saveMany(@RequestBody StudyEventHelper i) {
		log.debug("REST request to PUT size1=" + i.getData().size()
				+ ", size2=" + i.getDataOc().size());
		for (StudyEvent d : i.getData()) {
			log.debug(this.toString() + " sync id=" + d.getId());
			studyEventRepository.save(d);
		}
		for (StudyEventOC d : i.getDataOc()) {
			log.debug(this.toString() + " oc id=" + d.getId());
			d.save();
		}
	}

	private final Logger log = LoggerFactory
			.getLogger(StudyEventResource.class);

	@Inject
	private StudyEventRepository studyEventRepository;

	/**
	 * POST /studyEvents -> Create a new studyEvent.
	 */
	@RequestMapping(value = "/studyEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void create(@RequestBody StudyEvent studyEvent) {
		log.debug("REST request to save StudyEvent : {}", studyEvent);
		studyEventRepository.save(studyEvent);
	}

	/**
	 * GET /studyEvents -> get all the studyEvents.
	 */
	@RequestMapping(value = "/studyEvents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<StudyEvent> getAll() {
		log.debug("REST request to get all StudyEvents");
		return studyEventRepository.findAll();
	}

	/**
	 * GET /studyEvents/:id -> get the "id" studyEvent.
	 */
	@RequestMapping(value = "/studyEvents/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<StudyEvent> get(@PathVariable Long id,
			HttpServletResponse response) {
		log.debug("REST request to get StudyEvent : {}", id);
		StudyEvent studyEvent = studyEventRepository.findOne(id);
		if (studyEvent == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(studyEvent, HttpStatus.OK);
	}

	/**
	 * DELETE /studyEvents/:id -> delete the "id" studyEvent.
	 */
	@RequestMapping(value = "/studyEvents/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void delete(@PathVariable Long id) {
		log.debug("REST request to delete StudyEvent : {}", id);
		studyEventRepository.delete(id);
	}
}

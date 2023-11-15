package com.thf.dabplayer.dab;

import android.gesture.Prediction;
import com.thf.dabplayer.utils.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FicDecoder {

  private FIC_ENSEMBLE ensemble = new FIC_ENSEMBLE();
  private Map<Integer, FIC_SERVICE> services = new HashMap<>();
  private Map<Integer, FIC_SUBCHANNEL> subchannels = new HashMap<>();

  public void process(byte[] data, int len) {
    if (len % 32 != 0) {
      // System.err.printf("FICDecoder: Ignoring non-integer FIB count FIC data with %d bytes\n",
      // len);
      Logger.d(
          String.format(
              "FICDecoder: Ignoring non-integer FIB count FIC data with %d bytes\n", len));
      return;
    }
    for (int i = 0; i < len; i += 32) {
      ProcessFIB(Arrays.copyOfRange(data, i, i + 32));
    }
  }

  private void ProcessFIB(byte[] data) {
    int crc_stored = ((data[30] << 8) | data[31]);
    // short crc_calced = CalcCRC.CalcCRC_CRC16_CCITT.Calc(data, 30);
    int crc_calced = calculate_crc(data, 30);
    if (crc_stored != crc_calced) {
      Logger.d("FICDecoder: crc different");
      Logger.d("FICDecoder: crc stored: " + crc_stored);
      Logger.d("FICDecoder: crc calculated: " + crc_calced);
      return;
    }

    for (int offset = 0; offset < 30 && data[offset] != (byte) 0xFF; ) {
      int type = data[offset] >> 5;
      int len = data[offset] & 0x1F;
      offset++;
      switch (type) {
        case 0:
          Logger.d("FICDecoder: FIG0");
          ProcessFIG0(Arrays.copyOfRange(data, offset, offset + len), len);
          break;
        case 1:
          Logger.d("FICDecoder: FIG1");
          // ProcessFIG1(Arrays.copyOfRange(data, offset, offset + len), len);
          break;
      }
      offset += len;
    }
  }

  public static int calculate_crc(byte[] data, int len) {
    int dataIndex = 0;
    short c;
    short[] crc = {0};

    crc[0] = (short) 0xffff;
    for (int j = 1; j <= len; j++, dataIndex++) {
      c = (short) ((Short.toUnsignedInt(crc[0]) >> 8 ^ Byte.toUnsignedInt(data[dataIndex])) << 8);
      for (int i = 0; i < 8; i++) {
        if ((Short.toUnsignedInt(c) & 0x8000) != 0) {
          c = (short) (Short.toUnsignedInt(c) << 1 ^ 0x1021);
        } else {
          c = (short) (Short.toUnsignedInt(c) << 1);
        }
      }
      crc[0] = (short) (Short.toUnsignedInt(c) ^ Short.toUnsignedInt(crc[0]) << 8);
    }
    int crcInt = crc[0]; // & 0xffff;
    return ~crcInt;
  }

  class FIG0_HEADER {
    boolean cn;
    boolean oe;
    boolean pd;
    int extension;

    public FIG0_HEADER(byte data) {
      BitSet bitSet = BitSet.valueOf(new byte[] {data});
      cn = bitSet.get(7);
      oe = bitSet.get(6);
      pd = bitSet.get(5);
      BitSet extensionBits = bitSet.get(0, 5);
      extension = getIntFromBitSet(extensionBits);
    }

    private int getIntFromBitSet(BitSet bitSet) {
      int value = 0;
      for (int i = 0; i < bitSet.length(); i++) {
        if (bitSet.get(i)) {
          value += Math.pow(2, i);
        }
      }
      return value;
    }
  }

  class FIC_LABEL {
    private int charset;
    private byte[] label;
    private short short_label_mask;
    public static final int charset_none = -1;

    public FIC_LABEL() {
      charset = charset_none;
      short_label_mask = 0x0000;
      label = new byte[16];
      Arrays.fill(label, (byte) 0x00);
    }

    public boolean IsNone() {
      return charset == charset_none;
    }

    public boolean equals(FIC_LABEL fic_label) {
      return charset == fic_label.charset
          && Arrays.equals(label, fic_label.label)
          && short_label_mask == fic_label.short_label_mask;
    }

    public boolean notEquals(FIC_LABEL fic_label) {
      return !equals(fic_label);
    }
  }

  class AUDIO_SERVICE {
    private int subchid;
    private boolean dab_plus;
    public static final int subchid_none = -1;

    public AUDIO_SERVICE() {
      this(subchid_none, false);
    }

    public AUDIO_SERVICE(int subchid, boolean dab_plus) {
      this.subchid = subchid;
      this.dab_plus = dab_plus;
    }

    public boolean IsNone() {
      return subchid == subchid_none;
    }

    public boolean equals(AUDIO_SERVICE audio_service) {
      return subchid == audio_service.subchid && dab_plus == audio_service.dab_plus;
    }

    public boolean notEquals(AUDIO_SERVICE audio_service) {
      return !equals(audio_service);
    }
  }

  class FIC_ASW_CLUSTER {
    private short asw_flags;
    private int subchid;
    public static final int asw_flags_none = 0x0000;
    public static final int subchid_none = -1;

    public boolean IsNone() {
      return subchid == subchid_none;
    }

    public FIC_ASW_CLUSTER() {
      asw_flags = asw_flags_none;
      subchid = subchid_none;
    }

    public boolean equals(FIC_ASW_CLUSTER fic_asw_cluster) {
      return asw_flags == fic_asw_cluster.asw_flags && subchid == fic_asw_cluster.subchid;
    }

    public boolean notEquals(FIC_ASW_CLUSTER fic_asw_cluster) {
      return !equals(fic_asw_cluster);
    }
  }

  class FIC_ENSEMBLE {
    private int eid;
    private FIC_LABEL label;
    private int ecc;
    private int lto;
    private int inter_table_id;
    private Map<Byte, FIC_ASW_CLUSTER> asw_clusters;
    public static final int eid_none = -1;
    public static final int ecc_none = -1;
    public static final int lto_none = -100;
    public static final int inter_table_id_none = -1;

    public FIC_ENSEMBLE() {
      this.eid = eid_none;
      this.ecc = ecc_none;
      this.lto = lto_none;
      this.inter_table_id = inter_table_id_none;
      asw_clusters = new HashMap<>();
    }

    public boolean IsNone() {
      return eid == eid_none;
    }

    public boolean equals(FIC_ENSEMBLE ensemble) {
      return this.eid == ensemble.eid
          && Objects.equals(this.label, ensemble.label)
          && this.ecc == ensemble.ecc
          && this.lto == ensemble.lto
          && this.inter_table_id == ensemble.inter_table_id
          && Objects.equals(this.asw_clusters, ensemble.asw_clusters);
    }

    public boolean notEquals(FIC_ENSEMBLE ensemble) {
      return !this.equals(ensemble);
    }
  }

  void ProcessFIG0(byte[] data, int len) {
    if (len < 1) {
      System.err.println("FICDecoder: received empty FIG 0");
      return;
    }

    FIG0_HEADER header = new FIG0_HEADER(data[0]);
    data = Arrays.copyOfRange(data, 1, len);
    len--;

    if (header.cn || header.oe || header.pd) return;

    switch (header.extension) {
      case 1:
        // ProcessFIG0_1(data, len);
        break;
      case 2:
        // ProcessFIG0_2(data, len);
        break;
      case 5:
        // ProcessFIG0_5(data, len);
        break;
      case 8:
        // ProcessFIG0_8(data, len);
        break;
      case 9:
        ProcessFIG0_9(data, len);
        break;
      case 10:
        // ProcessFIG0_10(data, len);
        break;
      case 13:
        // ProcessFIG0_13(data, len);
        break;
      case 17:
        // ProcessFIG0_17(data, len);
        break;
      case 18:
        // ProcessFIG0_18(data, len);
        break;
      case 19:
        // ProcessFIG0_19(data, len);
        break;
    }
  }

  class FIC_SERVICE {
    public int sid;
    public int pri_comp_subchid;
    public FIC_LABEL label;
    public int pty_static;
    public int pty_dynamic;
    public short asu_flags;
    public Set<Byte> cids;
    public Map<Integer, AUDIO_SERVICE> audio_comps;
    public Map<Integer, Integer> comp_defs;
    public Map<Integer, FIC_LABEL> comp_labels;
    public Map<Integer, List<Byte>> comp_sls_uas;

    public static final int sid_none = -1;

    public boolean IsNone() {
      return sid == sid_none;
    }

    public static final int pri_comp_subchid_none = -1;

    public boolean HasNoPriCompSubchid() {
      return pri_comp_subchid == pri_comp_subchid_none;
    }

    public static final int pty_none = -1;

    public static final short asu_flags_none = 0x0000;

    public FIC_SERVICE() {
      sid = sid_none;
      pri_comp_subchid = pri_comp_subchid_none;
      pty_static = pty_none;
      pty_dynamic = pty_none;
      asu_flags = asu_flags_none;
      audio_comps = new HashMap<>();
      comp_defs = new HashMap<>();
      comp_labels = new HashMap<>();
      comp_sls_uas = new HashMap<>();
      cids = new HashSet<>();
    }
  }

  class FIC_SUBCHANNEL {
    public int start;
    public int size;
    public String pl;
    public int bitrate;
    public int language;
    public static final int language_none = -1;

    public boolean IsNone() {
      return pl.isEmpty() && language == language_none;
    }

    public FIC_SUBCHANNEL() {
      start = 0;
      size = 0;
      bitrate = -1;
      language = language_none;
    }

    public boolean equals(FIC_SUBCHANNEL fic_subchannel) {
      return start == fic_subchannel.start
          && size == fic_subchannel.size
          && Objects.equals(pl, fic_subchannel.pl)
          && bitrate == fic_subchannel.bitrate
          && language == fic_subchannel.language;
    }

    public boolean notEquals(FIC_SUBCHANNEL fic_subchannel) {
      return !equals(fic_subchannel);
    }
  }

  class LISTED_SERVICE {
    private int sid;
    private int scids;
    private FIC_SUBCHANNEL subchannel;
    private AUDIO_SERVICE audio_service;
    private FIC_LABEL label;
    private int pty_static;
    private int pty_dynamic;
    private int sls_app_type;
    private short asu_flags;
    public Set<Byte> cids;
    private int pri_comp_subchid; // only used for sorting
    private boolean multi_comps;

    public static final int sidNone = -1;

    public boolean isNone() {
      return sid == sidNone;
    }

    public static final int scids_none = -1;

    public boolean isPrimary() {
      return scids == scids_none;
    }

    public static final int pty_none = -1;

    public boolean isNone(int pty) {
      return pty == pty_none;
    }

    public static final int asu_flags_none = 0x0000;

    public boolean isNone(short asu_flags) {
      return asu_flags == asu_flags_none;
    }

    public static final int sls_app_type_none = -1;

    public boolean hasSLS() {
      return sls_app_type != sls_app_type_none;
    }

    public LISTED_SERVICE() {
      cids = new HashSet<>();
      sid = sidNone;
      scids = scids_none;
      pty_static = pty_none;
      pty_dynamic = pty_none;
      sls_app_type = sls_app_type_none;
      asu_flags = asu_flags_none;
      pri_comp_subchid = AUDIO_SERVICE.subchid_none;
      multi_comps = false;
    }

    public boolean compareTo(LISTED_SERVICE service) {
      if (pri_comp_subchid != service.pri_comp_subchid) {
        return pri_comp_subchid < service.pri_comp_subchid;
      }
      if (sid != service.sid) {
        return sid < service.sid;
      }
      return scids < service.scids;
    }
  }

  void ProcessFIG0_9(byte[] data, int len) {
    // FIG 0/9 - Time and country identifier - Country, LTO and International table
    // ensemble ECC/LTO and international table ID only

    if (len < 3) return;
    FIC_ENSEMBLE new_ensemble = ensemble;
    new_ensemble.lto = (data[0] & 0x20) != 0 ? -1 : 1 * (data[0] & 0x1F); // local time offset
    new_ensemble.ecc = data[1]; // extended country codes
    new_ensemble.inter_table_id = data[2];
    if (!ensemble.equals(new_ensemble)) {
      ensemble = new_ensemble;
      System.err.printf(
          "FICDecoder: ECC: 0x%02X, LTO: %s, international table ID: 0x%02X (%s)\n",
          ensemble.ecc,
          ConvertLTOToString(ensemble.lto),
          ensemble.inter_table_id,
          ConvertInterTableIDToString(ensemble.inter_table_id));
      UpdateEnsemble();
      for (Map.Entry<Integer, FIC_SERVICE> service : services.entrySet()) {
        FIC_SERVICE s = service.getValue();
        if (s.pty_static != FIC_SERVICE.pty_none || s.pty_dynamic != FIC_SERVICE.pty_none)
          UpdateService(s);
      }
    }
  }

  private void UpdateEnsemble() {
    // abort update, if label not yet present
    if (ensemble.label.IsNone()) return;

    // forward to observer
    // observer->FICChangeEnsemble(ensemble);
  }

  public static String ConvertLTOToString(int value) {
    char[] lto_string = new char[7];
    Formatter formatter = new Formatter();
    formatter.format("%+03d:%02d", value / 2, (value % 2) != 0 ? 30 : 0);
    String result = formatter.toString();
    formatter.close();
    return result;
  }

  public String ConvertInterTableIDToString(int value) {
    switch (value) {
      case 0x01:
        return "RDS PTY";
      case 0x02:
        return "RBDS PTY";
      default:
        return "unknown";
    }
  }

  public void UpdateService(FIC_SERVICE service) {
    if (service.HasNoPriCompSubchid() || service.label.IsNone()) {
      return;
    }
    boolean multi_comps = false;
    for (Map.Entry<Integer, Integer> comp_def : service.comp_defs.entrySet()) {
      if (comp_def.getValue() == service.pri_comp_subchid
          || !service.audio_comps.containsKey(comp_def.getValue())) {
        continue;
      }
      UpdateListedService(service, comp_def.getKey(), true);
      multi_comps = true;
    }
    UpdateListedService(service, LISTED_SERVICE.scids_none, multi_comps);
  }

  void UpdateListedService(FIC_SERVICE service, int scids, boolean multi_comps) {
    LISTED_SERVICE ls = new LISTED_SERVICE();

    ls.sid = service.sid;
    ls.scids = scids;
    ls.label = service.label;
    ls.pty_static = service.pty_static;
    ls.pty_dynamic = service.pty_dynamic;
    ls.asu_flags = service.asu_flags;
    ls.cids = service.cids;
    ls.pri_comp_subchid = service.pri_comp_subchid;
    ls.multi_comps = multi_comps;

    if (scids == LISTED_SERVICE.scids_none) {
      // primary component
      ls.audio_service = service.audio_comps.get(service.pri_comp_subchid);
    } else {
      // secondary component
      ls.audio_service = service.audio_comps.get(service.comp_defs.get(scids));

      // use component label, if available
      for (Map.Entry<Integer, FIC_LABEL> cl_it : service.comp_labels.entrySet()) {
        if (cl_it.getKey() == scids) {
          ls.label = cl_it.getValue();
          break;
        }
      }
    }
    // use sub-channel information, if available
    for (Map.Entry<Integer, FIC_SUBCHANNEL> sc_it : subchannels.entrySet()) {
      if (sc_it.getKey() == ls.audio_service.subchid) {
        ls.subchannel = sc_it.getValue();
        break;
      }
    }
    /* check (for) Slideshow; currently only supported in X-PAD
     * - derive the required SCIdS (if not yet known)
     * - derive app type from UA data (if present)
     */
    int sls_scids = scids;
    if (sls_scids == LISTED_SERVICE.scids_none) {
      for (Map.Entry<Integer, Integer> comp_def : service.comp_defs.entrySet()) {
        if (comp_def.getValue() == ls.audio_service.subchid) {
          sls_scids = comp_def.getKey();
          break;
        }
      }
    }

    if (sls_scids != LISTED_SERVICE.scids_none && service.comp_sls_uas.containsKey(sls_scids)) {
      ls.sls_app_type = GetSLSAppType(service.comp_sls_uas.get(sls_scids));
    }
  }

  public int GetSLSAppType(List<Byte> ua_data) {

    boolean ca_flag = false;
    int xpad_app_type = 12;
    boolean dg_flag = false;
    int dscty = 60;

    if (ua_data.size() >= 2) {
      ca_flag = (ua_data.get(0) & 0x80) != 0;
      xpad_app_type = ua_data.get(0) & 0x1F;
      dg_flag = (ua_data.get(1) & 0x80) != 0;
      dscty = ua_data.get(1) & 0x3F;
    }
    if (!ca_flag && !dg_flag && dscty == 60) {
      return xpad_app_type;
    } else {
      return LISTED_SERVICE.sls_app_type_none;
    }
  }

  class FIG1_HEADER {
    int charset;
    boolean oe;
    int extension;

    public FIG1_HEADER(byte data) {
      charset = (data >> 4) & 0xFF;
      int intOe = (data >> 3) & 0x1;
      oe = intOe == 1;
      extension = data & 0x7;
    }
  }

  public void ProcessFIG1(byte[] data, int len) {
    if (len < 1) {
      System.err.println("FICDecoder: received empty FIG 1");
      return;
    }

    FIG1_HEADER header = new FIG1_HEADER(data[0]);
    data = Arrays.copyOfRange(data, 1, len);
    len--;

    if (header.oe) return;

    int len_id = -1;
    switch (header.extension) {
      case 0: // ensemble
      case 1: //programme service
        len_id = 2;
        break;
      case 4: // service component
		      // programme services only (P/D = 0)
        if ((data[0] & 0x80) != 0) return;
        len_id = 3;
        break;
      default:
        return;
    }

    int len_calced = len_id + 16 + 2;
    if (len != len_calced) {
      System.err.printf(
          "FICDecoder: received FIG 1/%d having %d field bytes (expected: %d)\n",
          header.extension, len, len_calced);
      return;
    }

    FIC_LABEL label = new FIC_LABEL();
    label.charset = header.charset;
    System.arraycopy(data, len_id, label.label, 0, 16);
    label.short_label_mask = (short) ((data[len_id + 16] << 8) | data[len_id + 17]);

    // handle extension
    switch (header.extension) {
      case 0:
        {
          short eid = (short) ((data[0] << 8) | data[1]);
          // ProcessFIG1_0(eid, label);
          break;
        }
      case 1:
        {
          short sid = (short) ((data[0] << 8) | data[1]);
          // ProcessFIG1_1(sid, label);
          break;
        }
      case 4:
        {
          int scids = data[0] & 0x0F;
          short sid = (short) ((data[1] << 8) | data[2]);
          // ProcessFIG1_4(sid, scids, label);
          break;
        }
    }
  }
}

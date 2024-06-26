//
// Created by Andreas Auer on 19/03/24.
//

#ifndef PP_SOCKETHELPER_H
#define PP_SOCKETHELPER_H

#include "PacketStructure.h"
#include "configuration.h"
#include <cstring>
#include <iostream>
#include <list>
#include <netinet/in.h>
#include <queue>
#include <sys/socket.h>
#include <variant>

class SocketHelper {
private:
  std::queue<PacketVariant> packetQueue;
  uint16_t transmissionId = 0;
  bool msgSend = true;
  struct sockaddr_in serv_addr;
  struct sockaddr_in *dstIpAddr = nullptr;
  std::vector<Transmission *> incomingTransmission;
  std::string outputDir;
  bool verboseOutput;

  static void fillPacketHeader(PacketHeader *packetHeader, uint16_t tId,
                               uint32_t seqNum);
  static void fillPacket(Packet *packet, PacketHeader *packetHeader,
                         uint8_t *data, size_t dataLen);
  static void fillStartPacket(StartPacket *packet, PacketHeader *packetHeader,
                              size_t n, uint8_t *fileName, size_t nameLen);
  static void fillEndPacket(EndPacket *packet, PacketHeader *packetHeader,
                            const uint8_t *checksum);
  static void increaseSequenceNumber(PacketHeader *header);

  static void calcChecksum(EndPacket *endPacket, uint8_t *data, size_t dataLen,
                           uint8_t *fileName, size_t fileNameLen);
  static void calcChecksumFromTransmission(Transmission *transmission,
                                           EndPacket *endPacket);

  static bool checkCorrectnessOfPackets(StartPacket *startPacket,
                                        Packet *packets, EndPacket *endPacket);
  static bool checkCorrectnessOfTransmission(Transmission *t);

  bool pushToPacketQueue(PacketVariant packet);
  bool pushToIncomingQueue(char *buffer, ssize_t len);

  void createSocket(int *sock_fd, bool send);

  void sendPacket(int sock_fd, char *buffer, ssize_t len);
  void runMaster();
  void runSlave(const bool *run);

  void processIncomingMsg(Transmission *t);
  void checkFinishedTransmission();

  static void sortPackets(Packet *packets, size_t n);
  static void sortPackets(Transmission *t);

  bool savePacketsToFile(StartPacket *startPacket, Packet *packets);
  bool saveTransmissionToFile(Transmission *t);

public:
  void run(const bool *run, Config config);

  bool sendMsg(uint8_t *data, size_t dataLen, uint8_t *fileName,
               size_t fileNameLen);

  [[nodiscard]] bool msgOut() const;

  void setIpSettings(uint8_t *dstIp, size_t port);

  void setOutputDirPath(std::string outDir);

  void enableVerboseOutput() { verboseOutput = true; }
  void disableVerboseOutput() { verboseOutput = false; }
};

#endif // PP_SOCKETHELPER_H

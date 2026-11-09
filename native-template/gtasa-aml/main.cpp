#include "mod/amlmod.h"
#include "mod/logger.h"

MYMOD(net.sonix.sample, Sonix AML Sample, 1.0, SonixAI)

ON_MOD_PRELOAD()
{
    logger->SetTag("Sonix AML Sample");
}

ON_MOD_LOAD()
{
    logger->Info("Sonix AML sample loaded. No gameplay changes are active.");
}

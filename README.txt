The best way to run both projects is to use Eclipse's Import -> Existing Project functionality with the projects.

Game Engine
  Just run it in eclipse.
  The JRE I used when testing was JavaSE-1.8 (jre.1.8.0_25).
  I followed the given instructions from the link to add processing to eclipse.
  No arguments are required.

  Assignment1 is the first game, Game2 is the second.
  
  Run Client's main for the client and Server's main for the server.

  The Client and Server have a lot of code in common so I am just distributing them together
    However, they don't communicate other than through Sockets.
  You might notice a bunch of TODOs, please disregard these as they are notes for what I want to do in the later assignments.

  --NOTE ON RUNNING CLIENT--
  The new functionality works with both the Server-Client pair and the single client where it acts as its own server
    but runs smoother and cleaner if only the Client is run.
  Keys - wasd, x to "cheat" into fast moving for second project (script), k to "cheat" up and left (script) in the first.
  The second script prints on a death event.
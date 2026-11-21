import json
import pathlib
import unittest

BASE=pathlib.Path('src/main/java/io/github/santipdr/specialpickaxes')
class ConnectedExcavationTests(unittest.TestCase):
    def test_frontier_expands_only_after_native_break_or_confirmed_manual_scar(self):
        source=(BASE/'artifact/ConnectedExcavation.java').read_text()
        self.assertIn('p.getLookAngle().scale(6)',source)
        self.assertIn('ArtifactActions.loadedRayEnd',source)
        self.assertIn('delta.dot(look)>=0',source)
        self.assertIn('WorldSafety.mineNaturalLeased',source)
        self.assertIn('if(mined){',source)
        self.assertIn('seen.size()<cap*4',source)
        self.assertNotIn('setBlock(',source)

    def test_settling_visits_air_and_mined_cells_under_the_same_lease(self):
        source=(BASE/'artifact/ConnectedExcavation.java').read_text()
        self.assertIn('cleared.addLast(at)',source)
        self.assertIn('if(state.isAir()&&!revisit)',source)
        self.assertIn('settleUntil=Math.min(hardDeadline,now+120)',source)
        self.assertIn('ConnectedExcavation.this::active',source)
        self.assertIn('PlayerPlacedBlocks.get',source)
        self.assertIn('now>=hardDeadline',source)

    def test_runtime_native_and_equipped_routes_use_connected_mining(self):
        primary=(BASE/'artifact/ArtifactActions.java').read_text()
        alternate=(BASE/'artifact/ArtifactTechniques.java').read_text()
        equipped=(BASE/'artifact/CuriosMiningActive.java').read_text()
        self.assertIn('if(ConnectedExcavation.handles(kind))return ConnectedExcavation.start',primary)
        self.assertIn('return ConnectedExcavation.start(player,tool,kind,true)',alternate)
        self.assertIn('new ConnectedExcavation(p,at,kind,true,lease)',equipped)
        self.assertNotIn('new EventideRupture(',equipped)
        self.assertNotIn('new ParadoxLattice(',equipped)

    def test_visible_guides_are_short_positive_and_complete(self):
        from mining_catalog import IDS
        for language in ('en_us','es_es','es_uy'):
            data=json.loads(pathlib.Path('src/main/resources/assets/specialpickaxes/lang',language+'.json').read_text())
            for kind in IDS:
                for ability in ('main','alt'):
                    value=data[f'guide.brief.{ability}.{kind}']
                    self.assertLessEqual(len(value),160,(kind,language,ability))
                    for phrase in ('no radar','sin radar','no imán','no atrae','cannot','no teleport','no ore','celdas candidatas','candidate cells'):
                        self.assertNotIn(phrase,value.lower())

    def test_graphical_front_probe_and_fixed_deadline_capture_helper(self):
        fixture=(BASE/'test/ClientValidation.java').read_text()
        self.assertIn('new BlockPos(104,82,104)',fixture)
        self.assertIn('new BlockPos(112,82,149)',fixture)
        self.assertIn('removed<=4096',fixture)
        self.assertNotIn('wedgeState',fixture)
        manual=(BASE/'client/ArtifactManualScreen.java').read_text()
        self.assertIn('ConnectedExcavation.handles(abilityKind)?Component.translatable("guide.front")',manual)
        smoke=pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("['javac','-d',str(helper)",smoke)
        self.assertIn("'-cp',str(helper),'CaptureScreen'",smoke)
        self.assertIn('deadline=time.monotonic()+1100',smoke)
        hud=(BASE/'client/ArtifactHud.java').read_text()
        self.assertIn('@SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void levels',hud)

    def test_graphical_worldloom_never_collects_loot_or_resumes_for_the_player(self):
        source=(BASE/'test/ClientValidation.java').read_text()
        begin=source.index('if(current.kind()==ArtifactKind.WORLDLOOM)')
        end=source.index('if(current.kind()==ArtifactKind.INTERREGNUM',begin)
        observer=source[begin:end]
        self.assertNotIn('drop.discard()',observer)
        self.assertNotIn('WorkQueue.togglePause',observer)
        self.assertIn('throw new IllegalStateException("Worldloom paused for loot in the real client")',observer)
        self.assertIn('if(WorkQueue.busy(p))continue;',observer)
        self.assertIn('if(items<256',observer)
        self.assertIn('WORLDLOOM_NATIVE_LOOT_OK items=',observer)
        setup=source[source.index('if(k==ArtifactKind.WORLDLOOM)'):]
        self.assertIn('p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL)',setup)
        smoke=pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("if not any('WORLDLOOM_NATIVE_LOOT_OK ' in s for s in captured):raise SystemExit",smoke)

    def test_graphical_validation_keeps_pre_midnight_markers_and_errors(self):
        source=pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("runtime_logs=[out/'client.log',out/'console.log']",source)
        self.assertIn("*[str(log) for log in runtime_logs]",source)
        self.assertIn("join(log.read_text(errors='replace') for log in runtime_logs).lower()",source)
        self.assertNotIn("text=(out/'client.log').read_text().lower()",source)

    def test_real_survival_ascent_requires_excavation_through_two_ceilings(self):
        source=(BASE/'test/ClientValidation.java').read_text()
        flight=source[source.index('private static void verifyFlight('):source.index('private static void beginSupremeSetup(')]
        self.assertEqual(flight.count('for(int roofY:new int[]{104,108})'),2)
        self.assertIn('new BlockPos(0,roofY,14)).isAir()',flight)
        self.assertIn('new BlockPos(1,roofY,14)',flight)
        self.assertIn('Items.COBBLESTONE',flight)
        self.assertIn('getDouble("flightRise")<8',flight)
        smoke=pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("if not any('ICARUS_CEILINGS_OK' in s for s in captured):raise SystemExit",smoke)

    def test_terrain_cap_transitions_to_bounded_settling_instead_of_finishing(self):
        source=(BASE/'artifact/ConnectedExcavation.java').read_text()
        done=source[source.index('public boolean done()'):source.index('public int attemptsPerTick')]
        self.assertNotIn('broken>=cap',done)
        self.assertIn('settled>=Math.min(cap,1024)',done)
        self.assertIn('if(broken>=cap){',source)
        self.assertIn('frontier.clear();settleUntil=Math.min(hardDeadline,now+120)',source)
        self.assertIn('broken++;cleared.addLast(at)',source)

    def test_live_front_revisits_share_budget_and_cannot_finish_terrain_early(self):
        source=(BASE/'artifact/ConnectedExcavation.java').read_text()
        self.assertIn('frontAttemptsSinceRevisit>=15',source)
        self.assertIn('else frontAttemptsSinceRevisit++',source)
        self.assertIn('if(frontier.isEmpty())settleUntil=Math.min(hardDeadline,now+120)',source)
        self.assertIn('settled<Math.min(cap,1024)',source)
        done=source[source.index('public boolean done()'):source.index('public int attemptsPerTick')]
        self.assertIn('frontier.isEmpty()&&(settled>=Math.min(cap,1024)',done)

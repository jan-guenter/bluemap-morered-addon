# More Red visual gallery

This generated gallery covers all 45 registered More Red blocks. The 18 wire
families each get a connected floor pair; red alloy wire and bundled cable also
get floor crosses. Representative switched logic states and one stock control
round out the bounded comparison set.

Keep generated files synchronized with `cases.py` using these commands:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/morered-gallery.zip
```

Keep gallery generation deterministic, bounded, synthetic where practical, and
free of candidate assets or captured meshes.
